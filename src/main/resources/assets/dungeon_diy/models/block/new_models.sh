cp layout_block_white.json layout_block_$1.json
cp layout_block_white_corner.json layout_block_$1_corner.json
cp layout_block_white_four.json layout_block_$1_four.json
cp layout_block_white_hall.json layout_block_$1_hall.json
cp layout_block_white_one.json layout_block_$1_one.json
cp layout_block_white_three.json layout_block_$1_three.json

sed -i "s/white/$1/g" layout_block_$1*.json
