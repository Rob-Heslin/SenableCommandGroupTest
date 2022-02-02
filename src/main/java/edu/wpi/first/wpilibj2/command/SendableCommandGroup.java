// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package edu.wpi.first.wpilibj2.command;

import java.util.ArrayList;
import java.util.List;

import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

/**
 * A CommandGroups that runs a list of commands in sequence.
 * 
 *
 * <p>As a rule, CommandGroups require the union of the requirements of their component commands.
 */
public class SendableCommandGroup extends CommandGroupBase {
  private final List<Command> m_commands = new ArrayList<>();
//   private int m_currentCommandIndex = -1;
  private boolean m_runWhenDisabled = true;

  /**
   * Creates a new SendableCommandGroup. The given commands will be run sequentially, with the
   * CommandGroup finishing when the last command finishes.
   *
   * @param commands the commands to include in this group.
   */
  public SendableCommandGroup(Command... commands) {
    addCommands(commands);
  }

  @Override
  public final void addCommands(Command... commands) {
    if(this.getCurrentCommandIndex() == -20){
        this.setCurrentCommandIndex(-1);
    }

    //TODO: check to see if a different version of this commandgroup exists with different commands, throw error if true
    // if(SmartDashboard.getBoolean(this.getName() + "CommandExists", false)){
    //     throw new IllegalStateException(
    //         "SendableCommands can only be Constructed once");
    // }
    requireUngrouped(commands);

    if (SmartDashboard.getNumber(this.getName()+"CurrentCommandIndex",-1) != -1) {
      throw new IllegalStateException(
          "Commands cannot be added to a CommandGroup while the group is running");
    }

    registerGroupedCommands(commands);

    for (Command command : commands) {
      m_commands.add(command);
      m_requirements.addAll(command.getRequirements());
      m_runWhenDisabled &= command.runsWhenDisabled();
    }
  }

  @Override
  public void initialize() {
    if(getCurrentCommandIndex() <= -1) {
      setCurrentCommandIndex(0);
    }
  
    if (!m_commands.isEmpty()) {
      m_commands.get(getCurrentCommandIndex()).initialize();
    }
  }

  @Override
  public void execute() {
    if (m_commands.isEmpty()) {
      return;
    }

    Command currentCommand = m_commands.get(getCurrentCommandIndex());

    currentCommand.execute();
    if (currentCommand.isFinished()) {
      currentCommand.end(false);
      itterateCurrentCommandIndex();
      if (getCurrentCommandIndex() < m_commands.size()) {
        m_commands.get(getCurrentCommandIndex()).initialize();
      }
    }
  }

  @Override
  public void end(boolean interrupted) {
    if (interrupted
        && !m_commands.isEmpty()
        && getCurrentCommandIndex() > -1
        && getCurrentCommandIndex() < m_commands.size()) {
      m_commands.get(getCurrentCommandIndex()).end(true);
    }else{
      setCurrentCommandIndex(-1);
    }
  }

  @Override
  public boolean isFinished() {
    return getCurrentCommandIndex() == m_commands.size();
  }

  @Override
  public boolean runsWhenDisabled() {
    return m_runWhenDisabled;
  }

  protected final void setCurrentCommandIndex(int index){
    SmartDashboard.putNumber(this.getName()+"CurrentCommandIndex", index);
  }

  protected final int getCurrentCommandIndex(){
      return (int) SmartDashboard.getNumber(this.getName()+"CurrentCommandIndex",-20);
  }
  protected final void itterateCurrentCommandIndex(){
      setCurrentCommandIndex( getCurrentCommandIndex() + 1 );
  }

//   public final CommandBase itterateFoward(){
//       return new InstantCommand(()->{

//       });
//   }
}
