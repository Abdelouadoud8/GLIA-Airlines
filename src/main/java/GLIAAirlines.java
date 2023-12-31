import org.chocosolver.solver.Model;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.SetVar;
import java.util.ArrayList;
import java.util.List;


import static org.chocosolver.solver.search.strategy.Search.minDomLBSearch;
import static org.chocosolver.util.tools.ArrayUtils.append;

public class GLIAAirlines {


	IntVar[] dividers;

	Model model;



	public void solve(Instance inst, long timeout, boolean allSolutions) {

		buildModel(inst);
		model.getSolver().limitTime(timeout);
		StringBuilder st = new StringBuilder(
				String.format(model.getName() + "-- %s\n", inst.nb_dividers, " X ", inst.capacity));

		//solver call!
		model.getSolver().solve();

		model.getSolver().printStatistics();
		System.out.println("\nThe dividers of this solution are located in:");
		for (IntVar divider : dividers) {
			System.out.println(divider.getName() + " is located in position: " + divider.getValue());
		}

	}

	public void buildModel(Instance inst) {
		// A new model instance
		model = new Model("Aircraft Class Divider ");

		// VARIABLES
		dividers = model.intVarArray("dividers", inst.nb_dividers, 0, inst.capacity, false);

		// CONSTRAINTS
		//--------------- Constraint 1: Ensure dividers are distinct ---------------//
		/* BEFORE OPTIMISATION */
		//model.allDifferent(dividers).post();

		/* AFTER OPTIMISATION */
		model.allDifferent(dividers, "BC").post();


		//--------------- Constraint 2: Ensure dividers don't occupy exit positions ---------------//
		/* for (int exit : inst.exits) {
			model.arithm(dividers[0], "!=", exit).post();
		}*/

		if (inst.exits.length == 1) {
			for (int i = 0; i < inst.nb_dividers - 1; i++) {
				for (int exit : inst.exits) {
					model.arithm(dividers[i], "!=", exit).post();
				}
			}

		} else {
			List<Integer> unobstructedExits = new ArrayList<>();
			for (int exit : inst.exits) {
				boolean isObstructed = false;
				for (IntVar dividerPos : dividers) {
					if (dividerPos.getValue() == exit) {
						isObstructed = true;
						break;
					}
				}
				if (!isObstructed) {
					unobstructedExits.add(exit);
				}
			}

			// Ensure at least one exit remains unobstructed
			if (!unobstructedExits.isEmpty()) {
				int chosenExit = unobstructedExits.get(0);
				for (IntVar dividerPos : dividers) {
					model.arithm(dividerPos, "!=", chosenExit).post();
				}
			}
		}




		//---------- Constraint 3: Fix the first and the last dividers in the beginning and end of the plane ----------//
		/* BEFORE OPTIMISATION */
		//model.arithm(dividers[0], "=", 0).post();  // Fix first divider at position 0
		//model.arithm(dividers[inst.nb_dividers - 1], "=", inst.capacity).post();

		/* AFTER OPTIMISATION */
		model.element(dividers[0], new int[]{0}, dividers[0]).post();  // Fix first divider at position 0
		model.element(dividers[dividers.length - 1], new int[]{inst.capacity}, dividers[dividers.length - 1]).post();


		//------- Constraint 4: Ensure the front of the plane has at least two blocks and eliminate symetries -------//
		model.arithm(dividers[1], ">=", 2).post(); // Ensure second divider is after the second block
		for (int i = 1; i < inst.nb_dividers - 1; i++){
			model.arithm(dividers[i+1], ">", dividers[i]).post();
		}


		/*------- Constraint 5: Ensure that the dividers are placed in a way that we have different
		classes sizes, which means that distances between dividers are different -------*/
		/* BEFORE OPTIMISATION
		* ArrayList<IntVar> distances = new ArrayList<>();
			for (int i = 0; i < (inst.nb_dividers - 1); i++) {
				for (int j = i+1; j < inst.nb_dividers; j++) {
					IntVar diff = dividers[i].sub(dividers[j]).intVar();

					for (int k = 0; k < distances.size()-1; k++) {
						model.arithm(diff, "!=", distances.get(k)).post();
						//model.arithm(diff, "!=", dividers[i+1].sub(dividers[j+1]).intVar()).post();
						//model.arithm(diff, "!=", -distances[k]).post(); // Ensure the negative of the distance is also not repeated
					}
					distances.add(diff);
				}
			}
		* */


		// AFTER OPTIMISATION
		ArrayList<IntVar> distances = new ArrayList<>();
		for (int i = 0; i < (inst.nb_dividers - 1); i++) {
			for (int j = i + 1; j < inst.nb_dividers; j++) {
				IntVar diff = dividers[i].sub(dividers[j]).intVar();
				model.arithm(diff, "!=", 0).post(); // Ensure the distance is not zero
				distances.add(diff);
			}
		}

		IntVar[] distancesArray = new IntVar[distances.size()];
		distances.toArray(distancesArray);
		model.allDifferent(distancesArray).post();
	}


	public void configureSearch() {
		model.getSolver().setSearch(minDomLBSearch(append(dividers)));

	}


}