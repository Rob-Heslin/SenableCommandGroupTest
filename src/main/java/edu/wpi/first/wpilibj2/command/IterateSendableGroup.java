// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package edu.wpi.first.wpilibj2.command;

import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

/** Add your docs here. */
@Deprecated
public class IterateSendableGroup extends CommandBase{
    String name;
    boolean isForward;

    public IterateSendableGroup(SendableCommandGroup group, boolean isForward){
        name = group.getName()+"CurrentCommandIndex";
        System.out.println("I think the group i control is " + name);
        // this.isForward = isForward;
        
    }

    @Override
    public void initialize(){
        System.out.println("I ran at least a bit");
        int index = (int) SmartDashboard.getNumber(name,-1);
        System.out.println("I think the current commadn i got was " + index);
        SmartDashboard.putNumber(name, index+1);// (isForward?1:-1));
        System.out.println("I think the value i output to the smartdashboard is" + index );//+ (isForward?1:-1)));
    }

    @Override
    public boolean isFinished(){
        return true;
    }

    public boolean runsWhenDisabled(){
        return true;
    }
}
