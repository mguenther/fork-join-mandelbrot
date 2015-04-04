set terminal postscript eps "Helvetica" 12 enhanced color
set encoding iso_8859_1
set title "Mandelbrot @ i7-2700K, 3.50GHz, 4 cores with HT"
set output "optimal_parameterization.eps"
set logscale x
set logscale y
set xrange [1:128]
set yrange [1:2048]
set xtics (1,2,4,8,16,32,64,128)
set ytics (1,2,4,8,16,32,64,128,256,512,1024,2048)
set xlabel "worker threads [number]" rotate by 45 left
set ylabel "max. workload [number]"
set zlabel "execution time [ms]" rotate by 90 left
set surface
set pm3d

set view 60,50                 # rotates the view, so the results can be seen a lot better

splot "../resources/mandelbrot.dat" u 1:2:3 w linesp title "execution time"