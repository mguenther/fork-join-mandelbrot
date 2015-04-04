set terminal png size 1280,1024
set output "speedup_factor.png"
set size 1,1
set encoding iso_8859_1
set xlabel "worker threads [number]"
set ylabel "speedup factor [number]"
set logscale x
set logscale y
set xtics (1,2,4,8,16,32,64,128)
set ytics (1,2,4,8,16,32,64,128)
set xrange [1:128]
set yrange [1:16]
set grid
set title "Mandelbrot(32) @ i7-2700K, 3.50GHz, 4 cores with HT"
f(x)=x

plot '../resources/mandelbrot_speedup.dat' using 1:3 title "actual" with linesp lw 5, \
     f(x) with lines lw 2 title "ideal"
#     4 with lines lw 5 title "boundary"