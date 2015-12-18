http://opencvuser.blogspot.com/2011/08/creating-haar-cascade-classifier-aka.html
http://coding-robin.de/2013/07/22/train-your-own-opencv-haar-classifier.html
https://www.youtube.com/watch?v=WEzm7L5zoZE

find ./Negative_Images -name '*.ppm' >negatives.dat
find ./Positive_Images -name '*.png' >positives.dat

perl createtrainsamples.pl positives.dat negatives.dat samples 1500 "./opencv_createsamples  -bgcolor 0 -bgthresh 0 -maxxangle 1.1 -maxyangle 1.1 maxzangle 0.5 -maxidev 40 -w 160 -h 20"

find samples/ -iname info.dat -exec cat {} \; > out.txt

./opencv_createsamples -info out.txt -num 362 -w 60 -h 20 -vec glasses.vec

./opencv_createsamples -vec glasses.vec -w 60 -h 20

./opencv_traincascade  -data data -vec glasses.vec -bg negatives.dat -numPos 300 -numNeg 200 -numStages 40 -w 60 -h 20 -featureType LBP
