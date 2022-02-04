// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package edu.wpi.first.wpilibj2.command;

import java.util.ArrayList;
import java.util.List;

import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

/**
 * A CommandGroup that runs a list of commands in sequence.
 * 
 *
 * <p>As a rule, CommandGroups require the union of the requirements of their component commands.
 */
public class SendableCommandGroup extends CommandGroupBase {
  private final List<Command> m_commands = new ArrayList<>();
  private boolean m_runWhenDisabled = true;
  private int  m_lastCommandIndexExecuted = -1;
  protected String m_groupName;

  /**
   * Creates a new SendableCommandGroup. The given commands 
   * will be run sequentially, with the CommandGroup finishing 
   * when the last command finishes. This group is interuptable, 
   * and when interupted, the running command will end. But when 
   * this command is recalled, it will resume by restarting at 
   * the last command running. Additionally the current running 
   * command can be changed but using the sub-class commands.
   *
   * @param commands the commands to include in this group.
   */
  public SendableCommandGroup(Command... commands) {
    addCommands(commands);
  }

  @Override
  public final void addCommands(Command... commands) {
    m_groupName = this.getName();

    //if the currentCommandIndex is less than -1(say -20, the boot param) set the index to -1
    if(getCurrentCommandIndex() < -1){
        setCurrentCommandIndex(-1);
    }
    //ungroup commands like normal
    requireUngrouped(commands);

    //this is a CommandGroup, for later checks, needs more than one command
    // if( commands.length == 0 ){
    //   return;
    // }else if(commands.length == 1){
    //   throw new IllegalStateException(
    //     "SendableCommandGroups must contain at least 2 commands");
    // }

    //stop the addition of commands if the command is running
    if (getCurrentCommandIndex() > -1) {
      throw new IllegalStateException(
          "Commands cannot be added to a CommandGroup while the group is running");
    }
    // System.out.println("number of incoming commands: " + commands.length);
    //check to see if a different version of this commandGroup exists with different commands, throw error if true
    // String[] incomingCommandNames = new String[commands.length];
    // String[] existingCommandNames = SmartDashboard.getStringArray(m_groupName+"CommandList", new String[0]);
    // System.out.println(incomingCommandNames[0]+incomingCommandNames.length);//this was for testing
    // System.out.println(existingCommandNames[0]+existingCommandNames.length);//this was for testing
    // if( (existingCommandNames != null) ){
    //   if(existingCommandNames.length != incomingCommandNames.length){
    //     throw new IllegalStateException(
    //       "Identically named SendableCommandGroups can not have different numbers of commands");
    //   }
    //   int count = 0;
    //   for (Command command : commands) {
    //     incomingCommandNames[count]=command.getName();
    //     if(!(existingCommandNames[count].equals(incomingCommandNames[count]))){
    //       throw new IllegalStateException(
    //         "Identically named SendableCommandGroups can not have different commands or sequences of commands");
    //     }
    //     count++;
    //   }
    // }else{
    //   int count = 0;
    //   for (Command command : commands) {
    //     incomingCommandNames[count]=command.getName();
    //     count++;
    //   }
    // }

    registerGroupedCommands(commands);

    for (Command command : commands) {
      m_commands.add(command);
      m_requirements.addAll(command.getRequirements());
      m_runWhenDisabled &= command.runsWhenDisabled();
    }
    // SmartDashboard.putStringArray(m_groupName+"CommandList", incomingCommandNames);
    postCurrentCommand();
  }

  @Override
  public void initialize() {
    int currentCommandIndex = getCurrentCommandIndex();
    if(currentCommandIndex <= -1 
      ||currentCommandIndex >= m_commands.size()){
        
      setCurrentCommandIndex(0);
      currentCommandIndex = 0;
    }
  
    if (!m_commands.isEmpty()) {
      m_commands.get(currentCommandIndex).initialize();
    }
    postCurrentCommand();
    m_lastCommandIndexExecuted = currentCommandIndex;
  }

  @Override
  public void execute() {
    if (m_commands.isEmpty()) {
      return;
    }

    int currentCommandIndex = getCurrentCommandIndex();

    //check to see if the command index was chenged since the last loop(outside force)
    if( currentCommandIndex != m_lastCommandIndexExecuted ){
      //if so end previously runnning command
      m_commands.get(m_lastCommandIndexExecuted).end(true);
    }

    Command currentCommand = m_commands.get(currentCommandIndex);

    if( currentCommandIndex != m_lastCommandIndexExecuted){
      //again, if outside force has changed commandIndex, start the new commmand
      currentCommand.initialize();
    }

    currentCommand.execute();
    if (currentCommand.isFinished()) {
      currentCommand.end(false);
      iterateForwardCommandIndex();
      currentCommandIndex = getCurrentCommandIndex();
      if (currentCommandIndex < m_commands.size()) {
        m_commands.get(getCurrentCommandIndex()).initialize();
      }
    }
    //save the idex we last had, in case something outside chages it before execture is run again
    m_lastCommandIndexExecuted = currentCommandIndex;
  }

  @Override
  public void end(boolean interrupted) {
    //pull the current index we should be running
    int currentCommandIndex = getCurrentCommandIndex();
    if (interrupted
        && !m_commands.isEmpty()
        && currentCommandIndex > -1
        && currentCommandIndex < m_commands.size()) {
      //if we are interupted end the running command
      m_commands.get(currentCommandIndex).end(true);
    }else{
      //if we end peacefullly(and only if we end peacefully) reset the currentCommandindex
      setCurrentCommandIndex(-1);
    }
  }

  @Override
  public boolean isFinished() {
    return getCurrentCommandIndex() >= m_commands.size();
  }

  @Override
  public boolean runsWhenDisabled() {
    return m_runWhenDisabled;
  }

  protected final void setCurrentCommandIndex(int index){
    SmartDashboard.putNumber(getName()+"CurrentCommandIndex", index);
    postCurrentCommand();
  }

  /**
   * Pull the currentCommandIndex from the smart dashboard
   * @return
   */
  protected final int getCurrentCommandIndex(){
      return (int) SmartDashboard.getNumber(getName()+"CurrentCommandIndex",-20);
  }

  /**
   * increase the current command index by one, causing the the 
   * command that will be run to be one later in the sequence.
   * {@link postCurrentCommand} will be called after index is changed
   */
  protected final void iterateForwardCommandIndex(){
      setCurrentCommandIndex( getCurrentCommandIndex() + 1 );
      postCurrentCommand();
  }
  
  /**
   * decrease the current command index by one, causing the the 
   * command that will be run to be one earlier in the sequence.
   * {@link postCurrentCommand} will be called after index is changed
   */
  protected final void iterateBackwardCommandIndex(){
    setCurrentCommandIndex( getCurrentCommandIndex() - 1 );
    postCurrentCommand();
  }

  /**
   * this posted the name of the command that this command 
   * group will run to the SmartDashboard.
   */
  private final void postCurrentCommand(){
    String output = "";
    int currentIndex =  getCurrentCommandIndex();
    if( currentIndex <= -1 || currentIndex >= m_commands.size() ){
      output = "NOT_STARTED";
    }else{
      output = m_commands.get(currentIndex).getName();
    }
    SmartDashboard.putString(m_groupName + " Running:",output);
  }

  /**
   * 
   * @return a command that will put forward the commandIndex by one
   */
  public final CommandBase iterateFoward(){
      return new IterateCommand(true).withName(m_groupName+"ForwardOne");
  }

  /**
   * 
   * @return a command that will put backward the commandIndex by one
   */
  public final CommandBase iterateBackward(){
    return new IterateCommand(false).withName(m_groupName+"BackOne");
  }

  /**
   * Command Class to change the value of the commandIndex 
   * for the extended {@link SendableCommandGroup}
   */
  private class IterateCommand extends InstantCommand{
    private boolean isForward;
    public IterateCommand(boolean isForward){
      this.isForward = isForward;
    }
    @Override
    public void initialize(){
      if(isForward){
        //move the commandGroup forward once
        iterateForwardCommandIndex();
      }else{
        //move the commandGroup backward one
        iterateBackwardCommandIndex();
      }
    }

    public boolean runsWhenDisabled(){
      return true;//this is super important!!!! this command must run in disabled!!!
    }
  }
}
