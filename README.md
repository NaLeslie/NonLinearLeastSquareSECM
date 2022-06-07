# NonLinearLeastSquareSECM
Data-management software that refines guesses at primitive boundary conditions for mass transport models of scanning electrochemical microscopy (SECM) images.

Author: Nathaniel Leslie

Please note that this work is not currently at a point where it is "convenient" for use.

# Starting a fit:
1 - edit lines 207, 210, 225 232 of main.Demo.java to be the filepath for the experimental SECM image, a simulated SECM image of a disk with a similar radius and tip-to substrate distance, the file to which the centered secm image is to be written and the file to which the sub-sampled and centered experimental secm image is to be written.

2 - edit line 214 of main.Demo.java to reflect the minimum and maximum x and y coordinates from the experimental SECM image to be considered in the fitting/ alignment.

3 - edit lines 227 and 228 of main.Demo.java to determine which points exactly will be used during the fit.

4 - call setup_experimental_image() in the main.Main.main() method and run the program.

# First iteration:
1 - set iter to 0 on line 163 of main.Demo.java.

2 - set the initial guesses for r, logk, z in lines 165-167 of main.Demo.java.

3 - set diagonal to new double[]{0.0, 0.0, 0.0} on line 171 of main.Demo.java.

4 - set lambda to 0 on line 173 of main.Demo.java.

5 - set the filepath on line 162 of main.Demo.java to that of the sub-sampled and centered experimental secm image.

6 - set the filepath on line 164 of main.Demo.java to that of the simulated secm image using the initial guesses for r, logk, z.

7 - set the filepath of the output xml file containing the new parameters and jacobian diagonal on line 195 of main.Demo.java.

8 - call one_LRK_lm_iteration_exp() in the main.Main.main() method and run the program.

# Subsequent iterations:
1 - increment iter on line 163 of main.Demo.java.

2 - set the guesses for r, logk, z in lines 165-167 of main.Demo.java using the parameters in the previous iterations xml file.

3 - set diagonal on line 171 of main.Demo.java to the values in the previous iterations xml file.

4 - change lambda as needed on line 173 of main.Demo.java.

5 - set the filepath on line 164 of main.Demo.java to that of the simulated secm image for this iteration.

6 - set the filepath of the output xml file containing the new parameters and jacobian diagonal on line 195 of main.Demo.java.

7 - call one_LRK_lm_iteration_exp() in the main.Main.main() method and run the program.

# Input file formatting:
- Experimental SECM files are expected as comma separated ascii files with the format x,y,current

- Simulated SECM files are expected as comma separated ascii files with the format x,y,perturb_r,perturb_logk,perturb_z,current
