%% image preparation
clear all
close all
% pixelsize of the microscope (2.5x magnification)
Pixelsize=2.327270507812;

%loading image file
F1='C:OneDrive\Desktop\tesi_magistrale\fine_tuning\image_015';
f1=fullfile(F1);
ior=imread(f1,'tif');
% selecting only one channel
ior_g=double(ior(:,:,2));
% normalization and acquisition of image size
M=max(ior_g(:));
ior_g=ior_g*(1/M);
[Nx,Ny]=size(ior_g);

figure; imshow(ior)
figure; imshow(ior_g)
%% Wiener filter step
% spatial convolution through 3x3 kernel
imm=conv2(ior_g,ones(3),'same');

% local variance matrix
ivar=stdfilt(ior_g);
% noise variance
varianza=0.01;

% wiener filtration
iw=ior_g+((ivar-varianza)/ivar)*(imm-ior_g);
figure;
imshow(iw)
%% imfindcircles() step
% histogram equalization for enhancing contrast
iw_hist=histeq(iw);
figure;
imshow(iw_hist)

% binarization for improving computational performances
level=graythresh(iw_hist);
iw_BW=im2bw(iw_hist,level);

% Canny segmentation for reducing the number of pixels to consider as
% possible points of the circle
iw_edge=edge(iw_BW,'canny');
figure;
imshow(iw_edge)

% Hough transform in order to find the circle
[centers,radii]=imfindcircles(iw_edge,[380 450],"ObjectPolarity","bright","Sensitivity",0.995);
% converting pixels into micrometres
ceters_um=centers*Pixelsize;
radii_um=radii*Pixelsize;

% show and tell
figure;
imshow(ior_g)
hold on
viscircles(centers,radii,"EdgeColor",'r')
