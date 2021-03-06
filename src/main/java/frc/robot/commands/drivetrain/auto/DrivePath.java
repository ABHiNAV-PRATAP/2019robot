/*----------------------------------------------------------------------------*/
/* Copyright (c) 2018 FIRST. All Rights Reserved.                             */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package frc.robot.commands.drivetrain.auto;

import edu.wpi.first.wpilibj.command.Command;
import frc.robot.Robot;
import frc.robot.Constants;
import jaci.pathfinder.Pathfinder;
import jaci.pathfinder.Trajectory;
import jaci.pathfinder.followers.EncoderFollower;

public class DrivePath extends Command {
  /**
   * General command to drive a trajectory.
   */
  private Trajectory
    leftTrajectory,
    rightTrajectory;

  private EncoderFollower
    leftFollower,
    rightFollower;

  public DrivePath(Trajectory l, Trajectory r) {
    System.out.println("Running DrivePath");
    leftTrajectory = l;
    rightTrajectory = r;

    requires(Robot.drivetrain);
  }

  @Override
  protected void initialize() {
    System.out.println("Initializing DrivePath");

    Robot.drivetrain.getLeftEncoder().reset();
    Robot.drivetrain.getRightEncoder().reset();
    Robot.drivetrain.gyro.reset();

    leftFollower = new EncoderFollower(leftTrajectory);
    rightFollower = new EncoderFollower(rightTrajectory);

    leftFollower.configureEncoder(Robot.drivetrain.getLeftEncoderTicks(), Constants.kDriveEncoderTicksPerRev, Constants.kDriveWheelDiameter);
    rightFollower.configureEncoder(Robot.drivetrain.getLeftEncoderTicks(), Constants.kDriveEncoderTicksPerRev, Constants.kDriveWheelDiameter);
    leftFollower.configurePIDVA(Constants.kDriveP, Constants.kDriveI, Constants.kDriveD, 1 / Constants.kDriveVMax, Constants.kDriveA);
    rightFollower.configurePIDVA(Constants.kDriveP, Constants.kDriveI, Constants.kDriveD, 1 / Constants.kDriveVMax, Constants.kDriveA);

  }

  @Override
  protected void execute() {
    double leftSpeed = leftFollower.calculate(Robot.drivetrain.getLeftEncoderTicks());
    double rightSpeed = rightFollower.calculate(-Robot.drivetrain.getRightEncoderTicks());
    
    double gyroHeading = -Robot.drivetrain.getGyroAngle();
    double desiredHeading = Pathfinder.r2d(leftFollower.getHeading());
    double angleDifference = Pathfinder.boundHalfDegrees(desiredHeading - gyroHeading);
    double turn = 0.8 * (-1.0 / 80.0) * angleDifference;

    // System.out.println("leftSpeed : " + leftSpeed);
    // System.out.println("rightSpeed: " + rightSpeed);
    Robot.drivetrain.infuzedDrive(leftSpeed+turn, rightSpeed-turn);
  }

  @Override
  protected boolean isFinished() {
    return leftFollower.isFinished() || rightFollower.isFinished();
  }

  @Override
  protected void end() {
    Robot.drivetrain.stop();
  }

  @Override
  protected void interrupted() {
  }
}