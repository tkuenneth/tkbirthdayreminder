#!/bin/bash

# 1. Define your files
files=(
  "TKBirthdayReminder 01 en.png"
  "TKBirthdayReminder 02 en.png"
  "TKBirthdayReminder 03 en.png"
)

# 2. Setup index variables (macOS compatible)
count=${#files[@]}
last_index=$((count - 1))
first_file="${files[0]}"
last_file="${files[$last_index]}"

echo "--- Script Started ---"
CMD="convert -loop 0"

# 3. Loop for the internal transitions (A->B, B->C)
for ((i=0; i<last_index; i++)); do
   current="${files[i]}"
   next="${files[i+1]}"
   
   echo "  -> Adding transition: \"$current\" to \"$next\""
   CMD+=" \( -delay 100 \"$current\" \) \( -delay 10 \"$current\" \"$next\" -morph 10 \)"
done

# 4. Add the final hold
echo "  -> Adding final hold: \"$last_file\""
CMD+=" \( -delay 100 \"$last_file\" \)"

# 5. --- NEW STEP: CLOSE THE LOOP (C -> A) ---
#    We morph the last file back to the first file
echo "  -> Closing the loop: \"$last_file\" to \"$first_file\""
CMD+=" \( -delay 10 \"$last_file\" \"$first_file\" -morph 10 \)"

# 6. Output filename
CMD+=" TKBirthdayReminder-animated.gif"

echo "--- Command Building Complete ---"
echo "Starting ImageMagick..."

# 7. Execute
eval $CMD

echo "--- DONE ---"