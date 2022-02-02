// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package edu.wpi.first.wpilibj2.command;

import java.util.ArrayList;
import java.util.List;

import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;

/**
 * 
 *
 * 
 */
public class ResumableCommandGroup extends CommandGroupBase{
  //because these are static, you can extend this command once. not the purpose. will removing work?
  private static final List<Command> m_commands = new ArrayList<>();//The commands we will be running, in the order we will run them.
  private static int m_currentCommandIndex = -1;//what command we should be running right now, if not running, what command we should resume
  private boolean m_runWhenDisabled = true;
  private static String m_thisCommandName = "";

  /**
   * 
   *
   * @param commands the commands to include in this group.
   */
  public ResumableCommandGroup(Command... commands) {
    addCommands(commands);
  }

  @Override
  public final void addCommands(Command... commands) {
    requireUngrouped(commands);

    if (m_currentCommandIndex != -1) {
      throw new IllegalStateException(
          "Commands cannot be added to a CommandGroup while the group is running");
    }

    registerGroupedCommands(commands);
    
    
    for (Command command : commands) {
      m_commands.add(command);
      m_requirements.addAll(command.getRequirements());
      m_runWhenDisabled &= command.runsWhenDisabled();
    }
    m_thisCommandName = this.getName();
    postCurrentCommand();
  }
  
  @Override
  public void initialize() {
    if(m_currentCommandIndex <= -1) {
      m_currentCommandIndex = 0;
    }

    if (!m_commands.isEmpty()) {
      m_commands.get(m_currentCommandIndex).initialize();
    }
    postCurrentCommand();
  }

  @Override
  public void execute() {
    if (m_commands.isEmpty()) {
      return;
    }

    Command currentCommand = m_commands.get(m_currentCommandIndex);

    currentCommand.execute();
    if (currentCommand.isFinished()) {
      currentCommand.end(false);
      m_currentCommandIndex++;
      if (m_currentCommandIndex < m_commands.size()) {
        m_commands.get(m_currentCommandIndex).initialize();
        postCurrentCommand();
      }
    }
  }

  @Override
  public void end(boolean interrupted) {
    if (interrupted
        && !m_commands.isEmpty()
        && m_currentCommandIndex > -1
        && m_currentCommandIndex < m_commands.size()) {
      m_commands.get(m_currentCommandIndex).end(true);
    }else{
      m_currentCommandIndex = -1;
    }
    postCurrentCommand();
  }

  @Override
  public boolean isFinished() {
    return m_currentCommandIndex == m_commands.size();
  }

  @Override
  public boolean runsWhenDisabled() {
    return m_runWhenDisabled;
  }

  /**
   * this doesn't work
   * @return
   */
  public static CommandBase onePrev(){
    return new InstantCommand(
      () -> {
        System.out.println("got that first line");
        m_currentCommandIndex--;
        System.out.println("the index is " + m_currentCommandIndex);
        postCurrentCommand();
      }
      );//this can work without Requirements, but the scope of m_currentCommandIndex is a problem
  }

  // public static CommandBase callPrev(){
  //   return new CallPrev();
  // }

  /**
   * there isn't an easy way to call this
   */
  public static final void onePrevious(){
    m_currentCommandIndex--;
    postCurrentCommand();
  }

  /**
   * there isn't an easy way to call this
   */
  public static final void oneLater(){
    m_currentCommandIndex++;
    postCurrentCommand();
  }

  private static final void postCurrentCommand(){
    String output = "";
    if(m_currentCommandIndex <= -1){
      output = "NOT_STARTED";
    }else{
      output = m_currentCommandIndex + m_commands.get(m_currentCommandIndex).getName();
    }
    SmartDashboard.putString(m_thisCommandName + " Running:",output);
  }

  /**
   * this also doesn't work
   */
  class CallPrev extends CommandBase{
    @Override
    public void initialize() {
      System.out.println("got that first line");
      m_currentCommandIndex--;
      System.out.println("the index is " + m_currentCommandIndex);
      postCurrentCommand();
    }
    public boolean isFinished(){return true;}
  }
}
