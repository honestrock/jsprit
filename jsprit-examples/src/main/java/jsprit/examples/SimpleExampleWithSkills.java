/*******************************************************************************
 * Copyright (C) 2014  Stefan Schroeder
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public 
 * License along with this library.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package jsprit.examples;

import jsprit.analysis.toolbox.GraphStreamViewer;
import jsprit.analysis.toolbox.GraphStreamViewer.Label;
import jsprit.core.algorithm.VehicleRoutingAlgorithm;
import jsprit.core.algorithm.VehicleRoutingAlgorithmBuilder;
import jsprit.core.algorithm.state.StateManager;
import jsprit.core.problem.VehicleRoutingProblem;
import jsprit.core.problem.constraint.ConstraintManager;
import jsprit.core.problem.io.VrpXMLWriter;
import jsprit.core.problem.job.Service;
import jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import jsprit.core.problem.vehicle.VehicleImpl;
import jsprit.core.problem.vehicle.VehicleImpl.Builder;
import jsprit.core.problem.vehicle.VehicleType;
import jsprit.core.problem.vehicle.VehicleTypeImpl;
import jsprit.core.reporting.SolutionPrinter;
import jsprit.core.util.Coordinate;
import jsprit.core.util.Solutions;

import java.io.File;
import java.util.Collection;


public class SimpleExampleWithSkills {



	public static void main(String[] args) {
		/*
		 * some preparation - create output folder
		 */
		File dir = new File("output");
		// if the directory does not exist, create it
		if (!dir.exists()){
			System.out.println("creating directory ./output");
			boolean result = dir.mkdir();
			if(result) System.out.println("./output created");
		}

		/*
		 * get a vehicle type-builder and build a type with the typeId "vehicleType" and one capacity dimension, i.e. weight, and capacity dimension value of 2
		 */
		final int WEIGHT_INDEX = 0;
		VehicleTypeImpl.Builder vehicleTypeBuilder = VehicleTypeImpl.Builder.newInstance("vehicleType").addCapacityDimension(WEIGHT_INDEX, 2);
		VehicleType vehicleType = vehicleTypeBuilder.build();

		/*
		 * get a vehicle-builder and build a vehicle located at (10,10) with type "vehicleType"
		 */
		Builder vehicleBuilder = Builder.newInstance("vehicle");
		vehicleBuilder.setStartLocationCoordinate(Coordinate.newInstance(10, 10));
		vehicleBuilder.setType(vehicleType);
		VehicleImpl vehicle = vehicleBuilder.build();

        Builder vehicle2Builder = Builder.newInstance("vehicle2");
        vehicle2Builder.setStartLocationCoordinate(Coordinate.newInstance(1, 1));
        vehicle2Builder.setType(vehicleType);
        vehicle2Builder.addSkill("drill");
        VehicleImpl vehicle2 = vehicle2Builder.build();
		
		/*
		 * build services at the required locations, each with a capacity-demand of 1.
		 */
		Service service1 = Service.Builder.newInstance("1").addSizeDimension(WEIGHT_INDEX, 1).setCoord(Coordinate.newInstance(5, 7)).build();
		Service service2 = Service.Builder.newInstance("2").addSizeDimension(WEIGHT_INDEX, 1).setCoord(Coordinate.newInstance(5, 13)).build();
		
		Service service3 = Service.Builder.newInstance("3").addSizeDimension(WEIGHT_INDEX, 1).setCoord(Coordinate.newInstance(15, 7)).build();

		Service service4 = Service.Builder.newInstance("4").addSizeDimension(WEIGHT_INDEX, 1).addRequiredSkill("drill").setCoord(Coordinate.newInstance(15, 13)).build();
		
		
		VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
		vrpBuilder.addVehicle(vehicle).addVehicle(vehicle2);
		vrpBuilder.addJob(service1).addJob(service2).addJob(service3).addJob(service4);
		
		VehicleRoutingProblem problem = vrpBuilder.build();
		
		/*
		 * get the algorithm out-of-the-box. 
		 */
        VehicleRoutingAlgorithmBuilder vraBuilder = new VehicleRoutingAlgorithmBuilder(problem,"input/algorithmConfig.xml");
        vraBuilder.addCoreConstraints();
        vraBuilder.addDefaultCostCalculators();

        //activate skill state update and constraints - it is NOT default
        StateManager stateManager = new StateManager(problem);
        stateManager.updateSkillStates();

        ConstraintManager constraintManager = new ConstraintManager(problem,stateManager);
        constraintManager.addSkillsConstraint();

        vraBuilder.setStateAndConstraintManager(stateManager,constraintManager);

		VehicleRoutingAlgorithm algorithm = vraBuilder.build();
		
		/*
		 * and search a solution
		 */
		Collection<VehicleRoutingProblemSolution> solutions = algorithm.searchSolutions();
		
		/*
		 * get the best 
		 */
		VehicleRoutingProblemSolution bestSolution = Solutions.bestOf(solutions);
		
		new VrpXMLWriter(problem, solutions).write("output/problem-with-solution.xml");
		
		SolutionPrinter.print(problem, bestSolution, SolutionPrinter.Print.VERBOSE);
		
		/*
		 * plot
		 */
//		SolutionPlotter.plotSolutionAsPNG(problem, bestSolution, "output/solution.png", "solution");
		
		new GraphStreamViewer(problem, bestSolution).labelWith(Label.ID).setRenderDelay(200).display();
	}

}
