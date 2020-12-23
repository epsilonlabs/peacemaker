#!/usr/bin/env python

#%%
import pandas as pd
import sys
import matplotlib.pyplot as plt
import numpy as np

if len(sys.argv) == 2:
    filename = sys.argv[1]
else:
    filename = "../results/simpleTasksResults.csv"

c_elems = "numElements"
c_conflicts = "numConflicts"

c_pm = "Peacemaker"
c_emfcompare = "EMFCompare"
c_emfdiffmerge = "EMFDiffMerge"

measurements = [c_pm, c_emfcompare, c_emfdiffmerge]

#%%
df = pd.read_csv(filename)
df.head()

#%%
df = df.groupby([c_elems, c_conflicts])[measurements].agg(["mean", "std", "sem"]).reset_index().head(100)
df.head()

#%%
for measurement in measurements:
    df[measurement, "ci95_hi"] = df[measurement, "mean"] + 1.96 * df[measurement, "sem"]
    df[measurement, "ci95_lo"] = df[measurement, "mean"] - 1.96 * df[measurement, "sem"]

df.head()

#%%
# remove multi-level column index and export whole dataframe
df.columns = [col[0] if col[1] == "" else "_".join(col) for col in df.columns]
df.to_csv("{}_processed.csv".format(filename), index=False)


#%%
plt.style.use('seaborn-white')

plt.rc("font", family="serif")

SMALL_SIZE = 14
MEDIUM_SIZE = 16

plt.rc('font', size=SMALL_SIZE)          # controls default text sizes
plt.rc('axes', titlesize=22)     # fontsize of the axes title
plt.rc('axes', labelsize=MEDIUM_SIZE)    # fontsize of the x and y labels
plt.rc('xtick', labelsize=SMALL_SIZE)    # fontsize of the tick labels
plt.rc('ytick', labelsize=SMALL_SIZE)    # fontsize of the tick labels
plt.rc('legend', fontsize=SMALL_SIZE)    # legend fontsize

#%%
conflicts = df[c_conflicts].unique()

f = plt.figure(figsize=(6,6*len(conflicts)))
axes = f.subplots(nrows=len(conflicts), ncols=1)

# for 4 graphs
# f = plt.figure(figsize=(10,10))
# axes = [item for sublist in f.subplots(nrows=2, ncols=2) for item in sublist]

for conflict, ax in zip(conflicts, axes):
    df_conflict = df[df[c_conflicts] == conflict]
    ax.plot(df_conflict[c_elems],
            df_conflict["{}_mean".format(c_pm)],
            marker="o",
            label="Peacemaker")
    ax.plot(df_conflict[c_elems],
            df_conflict["{}_mean".format(c_emfcompare)],
            marker="^",
            label="EMF Compare")
    # ax.plot(df_conflict[c_elems],
    #         df_conflict["{}_mean".format(c_emfdiffmerge)],
    #         marker="s",
    #         label="EMF DiffMerge")
    ax.set_ylim(bottom=0)
    ax.set_xlabel("Number of elements")
    ax.set_ylabel("Conflict detection time (ms)")
    ax.set_title("{} Conflicts".format(conflict))
    ax.legend()

f.tight_layout()
f.savefig("{}_times.pdf".format(filename), bbox_inches='tight')
