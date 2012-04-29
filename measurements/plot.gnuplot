set terminal svg enhanced font "arial,10" size 500, 350 
set output 'mittaus.svg'

set title 'Execution time for /cs/fs/home/kerola/rio\_testdata/uint64-keys.bin'
set xlabel "Number of cores"
set ylabel "Execution time (msec)"
set mytics

plot 'quicksort.dat' title "Parallel quicksort" with lines \
   , 'mergesort.dat' title "Parallel mergesort" with lines \
   , 'serial_quicksort.dat' title 'Serial quicksort' with lines
