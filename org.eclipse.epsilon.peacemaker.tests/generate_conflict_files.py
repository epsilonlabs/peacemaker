#%%
import subprocess

#%%
tests = [["a1", ["conflict_a1_attribute", "conflict_a1_reference"]],
         ["a2", ["conflict_a2_attribute", "conflict_a2_reference"]],
         ["a3", ["conflict_a3_attribute", "conflict_a3_reference"]],

         ["b1", ["conflict_b1_attribute", "conflict_b1_reference"]],
         ["b2", ["conflict_b2_attribute", "conflict_b2_reference"]],
         ["b3", ["conflict_b3_attribute", "conflict_b3_reference",
                 "conflict_b3_containment_reference"]],
         ["b4", ["conflict_b4_attribute", "conflict_b4_reference"]],
         ["b5", ["conflict_b5_attribute", "conflict_b5_reference"]],
         ["b6", ["conflict_b6_attribute", "conflict_b6_reference"]],

         ["c1", ["conflict_c1_attribute", "conflict_c1_reference"]],
         ["c2", ["conflict_c2_attribute", "conflict_c2_reference"]],
         ["c3", ["conflict_c3_attribute", "conflict_c3_reference"]],
         ["c4", ["conflict_c4_attribute", "conflict_c4_reference"]],
         ["c5", ["conflict_c5_attribute", "conflict_c5_reference"]],

         ["d1", ["conflict_d1_attribute", "conflict_d1_reference"]],
         ["d2", ["conflict_d2_attribute", "conflict_d2_reference"]],
         ["d3", ["conflict_d3_attribute", "conflict_d3_reference"]],
         ["d4", ["conflict_d4_attribute", "conflict_d4_reference"]],
         ["d5", ["conflict_d5_attribute", "conflict_d5_reference"]],
         ["d6", ["conflict_d6_attribute", "conflict_d6_reference"]],

         ["e1", ["conflict_e1"]],
         ["e2", ["conflict_e2"]],
         ["e3", ["conflict_e3", "conflict_e3_avoidemptyroot"]],

         ["f", ["conflict_f"]],

         ["g", ["conflict_g"]],

         ["h1", ["conflict_h1"]],
         ["h2", ["conflict_h2"]],

         ["i", ["conflict_i"]],

         ["j", ["conflict_j"]],

         ["k1", ["conflict_k1"]],
         ["k2", ["conflict_k2"]],
         ["k3", ["conflict_k3"]],
         ["k4", ["conflict_k4"]]]

for folder, conflicts in tests:
    for conflict in conflicts:
        merged_file = "models/emfcompare-generated/{}_gitmerge.nodes".format(
                conflict.replace("conflict_", ""))
        with open(merged_file, "w") as gitmerge_file:
            subprocess.run(["git", "merge-file", "-p", "--diff3",
                            "models/emfcompare/{}/{}_left.nodes".format(folder, conflict),
                            "models/emfcompare/{}/{}_origin.nodes".format(folder, conflict),
                            "models/emfcompare/{}/{}_right.nodes".format(folder, conflict)],
                           stdout=gitmerge_file)

print("done")
