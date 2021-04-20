# Only works for EMF Compare cases
# $1 - prefix of the conflict case

origin=$1origin.nodes
left=$1left.nodes
right=$1right.nodes


echo "ORIGIN"
cat $origin
echo ""

echo "LEFT"
cat $left
echo ""

echo "RIGHT"
cat $right
echo ""


echo "LEFT DIFF WITH ORIGIN"
git diff --no-index $origin $left
echo ""

echo "RIGHT DIFF WITH ORIGIN"
git diff --no-index $origin $right
echo ""

echo "MERGE RESULT"
git merge-file -p --diff3 $left $origin $right
