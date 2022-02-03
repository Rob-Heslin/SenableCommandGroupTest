// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.commands;

import edu.wpi.first.wpilibj2.command.PrintCommand;
import edu.wpi.first.wpilibj2.command.ResumableCommandGroup;
import edu.wpi.first.wpilibj2.command.SendableCommandGroup;
import edu.wpi.first.wpilibj2.command.WaitCommand;

// NOTE:  Consider using this command inline, rather than writing a subclass.  For more
// information, see:
// https://docs.wpilib.org/en/stable/docs/software/commandbased/convenience-features.html
public class TestGroup extends SendableCommandGroup {
  /** Creates a new TestGroup. */
  public TestGroup() {
    // Add your commands in the addCommands() call, e.g.
    // addCommands(new FooCommand(), new BarCommand());
    addCommands(
      new WaitCommand(3).withName("Steve"),
      new PrintCommand("SecondCommand").withName("Mjolner"),
      new WaitCommand(4).withName("Josh"),
      new PrintCommand("FourthCommand").withName("Tuba"),
      new WaitCommand(3.5).withName("UpDog")
    );
  }
}
