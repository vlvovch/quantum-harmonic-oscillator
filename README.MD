<img src="app/src/main/res/mipmap-xxxhdpi/ic_launcher.png" align="right" />

# Quantum Harmonic Oscillator 

<a href='https://play.google.com/store/apps/details?id=com.vlvolad.quantumoscillator'><img alt='Get it on Google Play' src='https://play.google.com/intl/en_us/badges/images/generic/en_badge_web_generic.png' height=60px/></a>

This repository contains the source code of the [Quantum Harmonic Oscillator app for Android](https://play.google.com/store/apps/details?id=com.vlvolad.quantumoscillator)


The code is designed to be used via Android Studio with Gradle.

## Short description of the app 


The app visualizes the electron orbitals of the 3D quantum harmonic oscillator by using OpenGL.
More specifically, the hypersurfaces of constant spatial electron probability density of the different orbitals are drawn, exploiting the known exact solution of the Schroedinger equation.

The quantum numbers *n*, *l*, and *m*, the probability *P* to find electron inside the orbital, and the discretization level can all be varied.
Choice between complex and real (in azimuthal angle) basis of wave functions is possible.


The visualization is performed by dynamically generating the hypersurfaces with the [Marching Cubes](http://paulbourke.net/geometry/polygonise/) algorithm. Orbitals can be interactively zoomed and rotated.

This is a sister app of [Hydrogen Atom Orbitals](https://github.com/vlvovch/hydrogen-atom-orbitals)
