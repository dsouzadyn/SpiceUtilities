
package spice.basic;

/**
Class TerminatorPoint supports terminator point computations.

<p> TerminatorPoint instances consist of
<ul>
<li> An inherited {@link Vector3} instance representing
a terminator point. </li>

<li> The epoch of participation of the target body. 

<p>  This is
  the observation epoch, minus the approximate one-way
  light time from the terminator point to the observer, if aberration
  corrections are used. The way the light time is computed
  depends on the choice of aberration correction locus.
</li>

<li> A vector from the observer to the terminator point, expressed
  in the target body-fixed reference frame, evaluated at the
  epoch of participation of the target body.
</li>
</ul>
</pre>

<p> See the detailed documentation of the method 
{@link #create} for code examples.



<h2>Files</h2>

<p>Appropriate SPICE kernels must be loaded by the calling program
   before methods of this class are called.

<p>The following data are required:

<ul>
<li>
        SPK data: the calling application must load ephemeris data
        for the target, illumination source, and observer. If aberration
        corrections are used, the states of the target body, illumination
        source, and observer relative to the solar system barycenter must be
        calculable from the available ephemeris data. Typically
        ephemeris data are made available by loading one or more SPK
        files via {@link KernelDatabase#load}.
</li>
<li> PCK data: bodies modeled as triaxial ellipsoids must have
        semi-axis lengths provided by variables in the kernel pool.
        Typically these data are made available by loading a text
        PCK file via {@link KernelDatabase#load}.
</li>
<li>
        FK data: if the reference frame designated by `fixref'
        is not built in to the SPICE system, an FK specifying 
        this frame must be loaded.
</li>
</ul>

<p>
   The following data may be required:
<ul>
<li>    DSK data: if `method' indicates that DSK
        data are to be used, DSK files containing topographic data
        for the target body must be loaded. If a surface list is
        specified, data for at least one of the listed surfaces must
        be loaded.

<p>
        Surface name-ID associations: if surface names are specified
        in `method', the association of these names with
        their corresponding surface ID codes must be established by
        assignments of the kernel variables
<pre>
   NAIF_SURFACE_NAME
   NAIF_SURFACE_CODE
   NAIF_SURFACE_BODY
</pre>
        Normally these associations are made by loading a text
        kernel containing the necessary assignments. An example
        of such a set of assignments is
<pre>
   NAIF_SURFACE_NAME += 'Mars MEGDR 128 PIXEL/DEG'
   NAIF_SURFACE_CODE += 1
   NAIF_SURFACE_BODY += 499
</pre>
      
<li>
        CK data: the body-fixed frame to which `fixref'
        refers might be a CK frame. If so, at least one CK
        file will be needed to permit transformation of vectors
        between that frame and the J2000 frame.
</li>
<li>
        SCLK data: if a CK file is needed, an associated SCLK
        kernel is required to enable conversion between encoded SCLK
        (used to time-tag CK data) and barycentric dynamical time
        (TDB).
</li>
</ul>
<p>
   Kernel data are normally loaded once per program run, NOT every
   time a method of this class is called.



<h2> Class TerminatorPoint Particulars </h2>

<h3>Using DSK data</h3>

 
<p><b>DSK loading and unloading</b>
 
<p>DSK files providing data used by this class are loaded by 
      calling {@link KernelDatabase#load} and can be unloaded by 
      calling {@link KernelDatabase#unload} or
      {@link KernelDatabase#clear}. See the documentation of 
      {@link KernelDatabase#load} for limits on numbers 
      of loaded DSK files. 
 
      For run-time efficiency, it's desirable to avoid frequent 
      loading and unloading of DSK files. When there is a reason to 
      use multiple versions of data for a given target body---for 
      example, if topographic data at varying resolutions are to be 
      used---the surface list can be used to select DSK data to be 
      used for a given computation. It is not necessary to unload 
      the data that are not to be used. This recommendation presumes 
      that DSKs containing different versions of surface data for a 
      given body have different surface ID codes. 
 
 
<p><b>DSK data priority</b>

 
<p>   A DSK coverage overlap occurs when two segments in loaded DSK 
      files cover part or all of the same domain---for example, a 
      given longitude-latitude rectangle---and when the time 
      intervals of the segments overlap as well. 
 
<p>   When DSK data selection is prioritized, in case of a coverage 
      overlap, if the two competing segments are in different DSK 
      files, the segment in the DSK file loaded last takes 
      precedence. If the two segments are in the same file, the 
      segment located closer to the end of the file takes 
      precedence. 
 
<p>   When DSK data selection is unprioritized, data from competing 
      segments are combined. For example, if two competing segments 
      both represent a surface as a set of triangular plates, the 
      union of those sets of plates is considered to represent the 
      surface.  
 
<p>   Currently only unprioritized data selection is supported. 
      Because prioritized data selection may be the default behavior 
      in a later version of the routine, the UNPRIORITIZED keyword is 
      required in the `method' argument. 




<h3> Version 1.0.0 06-DEC-2016 (NJB) </h3>

*/
public class TerminatorPoint extends Vector3
{
   //
   // Public Constants
   //

   //
   // Fields
   //
   private TDBTime          targetEpoch;
   private Vector3          surfaceVector;


   //
   // Constructors
   //

   /**
   Create a terminator point from a surface point, epoch, and
   observer-to terminator point vector.
   */
   public TerminatorPoint ( Vector3      terminatorPoint,
                            Time         targetEpoch,
                            Vector3      surfaceVector )

      throws SpiceException
   {
      super( terminatorPoint );

      this.targetEpoch     = new TDBTime( targetEpoch     );
      this.surfaceVector   = new Vector3( surfaceVector   );
   }


   /**
   Copy constructor.
   */
   public TerminatorPoint ( TerminatorPoint   tpoint )

      throws SpiceException
   {
      super( tpoint );

      this.targetEpoch     = new TDBTime( tpoint.targetEpoch     );
      this.surfaceVector   = new Vector3( tpoint.surfaceVector   );
   }


   /**
   No-arguments constructor.
   */
   public TerminatorPoint ()
   {
      super();
   }


   //
   // Methods
   //
 
   /**
   Create an array of terminator points on a specified target body, 
   as seen from a specified observer.

   <p> This is the principal method for constructing a representation
   of a terminator.

   <p> In the returned two-dimensional array, the ith row contains the
   terminator points for the ith cutting half-plane. The rows do not 
   necessarily have equal length. 
      
   <h2> Inputs </h2>
   
<pre>
   method   is a String instance providing parameters defining 
            the computation method to be used. In the syntax 
            descriptions below, items delimited by brackets 
            "[]" are optional. 
 
            `method' may be assigned the following values: 
 
               "<shadow>/<curve type>/<shape specification>" 
 
            An example of such a string is  
 
               "UMBRAL/TANGENT/DSK/UNPRIORITIZED" 
 
            In the `method' string 
 
               <shadow> may be either of the strings 
 
                  "UMBRAL"    indicates the terminator is the 
                              boundary of the portion of the surface 
                              that receives no light from the 
                              illumination source. The shape of 
                              the source is modeled as a sphere. 
 
                  "PENUMBRAL" indicates the terminator is the 
                              boundary of the portion of the 
                              surface that receives all possible 
                              light from the illumination source. 
                              The shape of the source is modeled as 
                              a sphere. 
 
                              The penumbral terminator bounds the 
                              portion of the surface that is not 
                              subject to self-occultation of light 
                              from the illumination source. Given 
                              that the light source is modeled as a 
                              sphere, from any target surface point 
                              nearer to the source than the 
                              penumbral terminator, the source 
                              appears to be a lit disc. 


               <curve type> may be either of the strings   

                  "TANGENT"   for topographic (DSK) target models
                              indicates that a terminator point is
                              defined as the point of tangency, on
                              the surface represented by the
                              specified data, of a line also tangent
                              to the illumination source. For
                              ellipsoidal target models, a
                              terminator point is a point of
                              tangency of a plane that is also
                              tangent to the illumination source.
                              See the Particulars section below for
                              details.
 
                              This is the highest-accuracy method 
                              supported by this subroutine. It 
                              generally executes much more slowly 
                              than the GUIDED method described 
                              below. 
 
                  "GUIDED"    indicates that terminator points are 
                              "guided" so as to lie on rays 
                              emanating from the target body's 
                              center and passing through the 
                              terminator on of the target body's 
                              reference ellipsoid. The terminator 
                              points are constrained to lie on the 
                              target body's surface. As with the 
                              "TANGENT" method (see above), cutting 
                              half-planes are used to generate 
                              terminator points. 
 
                              The GUIDED method produces a unique 
                              terminator point for each cutting 
                              half-plane. If multiple terminator 
                              point candidates lie in a given 
                              cutting half-plane, the outermost one 
                              is chosen. 
 
                              This method may be used only with the 
                              CENTER aberration correction locus 
                              (see the description of REFLOC below). 
 
                              Terminator points generated by this 
                              method are approximations; they are 
                              generally not true ray-surface tangent 
                              points. However, these approximations 
                              can be generated much more quickly 
                              than tangent points. 
 
 
               <shape specification> may be either of the strings 
 
                  "DSK/UNPRIORITIZED[/SURFACES = <surface list>]" 
 
                     The DSK option indicates that terminator point 
                     computation uses topographic data provided by 
                     DSK files (abbreviated as "DSK data" below) to 
                     model the surface of the target body. 
  
                     The surface list specification is optional. The 
                     syntax of the list is 
 
                        <surface 1> [, <surface 2>...]
 
                     If present, it indicates that data only for the 
                     listed surfaces are to be used; however, data 
                     need not be available for all surfaces in the 
                     list. If the list is absent, loaded DSK data 
                     for any surface associated with the target body 
                     are used. 
 
                     The surface list may contain surface names or 
                     surface ID codes. Names containing blanks must 
                     be delimited by double quotes, for example 
 
                        "SURFACES = \"Mars MEGDR 128 PIXEL/DEG\""
 
                     If multiple surfaces are specified, their names 
                     or IDs must be separated by commas. 
 
                     See the Particulars section below for details 
                     concerning use of DSK data. 
 
                   
                  "ELLIPSOID" 
 
                     The ELLIPSOID shape option generates terminator 
                     points on the target body's reference 
                     ellipsoid. When the ELLIPSOID shape is 
                     selected, The TANGENT curve option may be used 
                     with any aberration correction locus, while the 
                     GUIDED option may be used only with the CENTER 
                     locus (see the description of REFLOC below). 
 
                     When the locus is set to "CENTER", the 
                     "TANGENT" and "GUIDED" curve options produce 
                     the same results. 
 
               Neither case nor white space are significant in 
               `method', except within double-quoted strings. For 
               example, the string " eLLipsoid/tAnGenT " is valid. 
 
               Within double-quoted strings, blank characters are 
               significant, but multiple consecutive blanks are 
               considered equivalent to a single blank. Case is  
               not significant. So 
 
                  \"Mars MEGDR 128 PIXEL/DEG\" 
 
               is equivalent to  
 
                  \" mars megdr  128  pixel/deg \" 
 
               but not to 
 
                  \"MARS MEGDR128PIXEL/DEG\" 
 
                
   ilusrc      is a {@link Body} instance identifying the illumination source. 
               This source may be any extended ephemeris object that is 
               modeled as a triaxial ellipsoid.
 
               The shape of the illumination source is considered 
               to be spherical. The radius of the sphere is the 
               largest radius of the source's reference ellipsoid. 
 

   target      is a {@link Body} instance identifying the target body. The 
               target body is an extended ephemeris object. 
  
               When the target body's surface is represented by a 
               tri-axial ellipsoid, this routine assumes that a 
               kernel variable representing the ellipsoid's radii is 
               present in the kernel pool. Normally the kernel 
               variable would be defined by loading a PCK file. 
 
 
   et          is a {@link Time} instance representing the epoch of 
               participation of the observer: `et' is the epoch at 
               which the observer's state is computed. 
 
               When aberration corrections are not used, `et' is also 
               the epoch at which the position and orientation of 
               the target body are computed. 
 
               When aberration corrections are used, the position 
               and orientation of the target body are computed at 
               et-lt, where lt is the one-way light time between the 
               aberration correction locus and the observer. The 
               locus is specified by the input argument `corloc'. 
               See the descriptions of `abcorr' and `corloc' below for 
               details. 
 
 
   fixref      is a {@link ReferenceFrame} instance representing
               a body-fixed reference frame centered 
               on the target body. `fixref' may be any such frame 
               supported by the SPICE system, including built-in 
               frames (documented in the Frames Required Reading) 
               and frames defined by a loaded frame kernel (FK).  
 
               The output terminator points and observer-target surface
               vectors in the returned TerminatorPoint array are expressed 
               relative to this reference frame. 
 
 
   abcorr      is an {@link AberrationCorrection} instance that
               indicates the aberration corrections to be applied 
               when computing the target's position and orientation,
               as well as the position of the illumination source. 
               Corrections for the target are applied at the location 
               specified by the aberration correction locus argument `corloc', 
               which is described below. 
 
               For remote sensing applications, where apparent terminator 
               points seen by the observer are desired, normally 
               either of the corrections 
             
                  "LT+S"  
                  "CN+S" 
    
               should be used. The correction "NONE" may be suitable 
               for cases in which the target is very small and the 
               observer is close to, and has small velocity relative 
               to, the target (e.g. comet Churyumov-Gerasimenko and 
               the Rosetta Orbiter). 
 
               These and the other supported options are described 
               below. `abcorr' may be any of the following: 
 
                  "NONE"     Apply no correction. Return the 
                             geometric terminator points on the target 
                             body. 
 
               Let `lt' represent the one-way light time between the 
               observer and the aberration correction locus. The 
               following values of `abcorr' apply to the "reception" 
               case in which photons depart from the locus at the 
               light-time corrected epoch et-lt and *arrive* at the 
               observer's location at `et': 
 
 
                  "LT"       Correct for one-way light time (also 
                             called "planetary aberration") using a 
                             Newtonian formulation. This correction 
                             yields the locus at the moment it 
                             emitted photons arriving at the 
                             observer at `et'. 
  
                             The light time correction uses an 
                             iterative solution of the light time 
                             equation. The solution invoked by the 
                             "LT" option uses one iteration. 
 
                             Both the target position as seen by the 
                             observer, and rotation of the target 
                             body, are corrected for light time. The
                             position of the illumination source as
                             seen from the target is corrected as
                             well.
 
                  "LT+S"     Correct for one-way light time and 
                             stellar aberration using a Newtonian 
                             formulation. This option modifies the 
                             locus obtained with the "LT" option to 
                             account for the observer's velocity 
                             relative to the solar system 
                             barycenter. These corrections yield 
                             points on the apparent terminator. 
 
                  "CN"       Converged Newtonian light time 
                             correction. In solving the light time 
                             equation, the "CN" correction iterates 
                             until the solution converges. Both the 
                             position and rotation of the target 
                             body are corrected for light time. 
 
                  "CN+S"     Converged Newtonian light time and 
                             stellar aberration corrections. This 
                             option produces a solution that is at 
                             least as accurate at that obtainable 
                             with the "LT+S" option. Whether the 
                             "CN+S" solution is substantially more 
                             accurate depends on the geometry of the 
                             participating objects and on the 
                             accuracy of the input data. In all 
                             cases this routine will execute more 
                             slowly when a converged solution is 
                             computed. 
 
 
   corloc      is a String specifying the aberration correction 
               locus: the point or set of points for which 
               aberration corrections are performed. `corloc' may be 
               assigned the values: 
 
                  "CENTER"  
 
                      Light time and stellar aberration corrections 
                      are applied to the vector from the observer to 
                      the center of the target body. The one way 
                      light time from the target center to the 
                      observer is used to determine the epoch at 
                      which the target body orientation is computed. 
 
                      This choice is appropriate for small target 
                      objects for which the light time from the 
                      surface to the observer varies little across 
                      the entire target. It may also be appropriate 
                      for large, nearly ellipsoidal targets when the 
                      observer is very far from the target. 
 
                      Computation speed for this option is faster 
                      than for the "ELLIPSOID TERMINATOR" option. 
 
                  "ELLIPSOID TERMINATOR" 
 
                      Light time and stellar aberration corrections 
                      are applied to individual terminator points on the 
                      reference ellipsoid. For a terminator point on the 
                      surface described by topographic data, lying 
                      in a specified cutting half-plane, the unique 
                      reference ellipsoid terminator point in the same 
                      half-plane is used as the locus of the 
                      aberration corrections. 
 
                      This choice is appropriate for large target 
                      objects for which the light time from the terminator 
                      to the observer is significantly different 
                      from the light time from the target center to 
                      the observer. 
 
                      Because aberration corrections are repeated for 
                      individual terminator points, computational speed for 
                      this option is relatively slow. 
 
 
   obsrvr      is a {@link Body} instance identifying
               the observing body. The observing body 
               is an ephemeris object: it typically is a spacecraft, 
               the earth, or a surface point on the earth.  
 
 
   refvec, 
   rolstp, 
   ncuts       are, respectively, a reference vector, a roll step 
               angle, and a count of cutting half-planes. 
 
               `refvec' is a {@link Vector3} instance that
               defines the first of a sequence of cutting 
               half-planes in which terminator points are to be found. 
               Each cutting half-plane has as its edge the line 
               containing the target-illumination source vector; 
               the first half-plane contains `refvec'. 
 
               `refvec' is expressed in the body-fixed reference frame 
               designated by `fixref'. 
 
               `rolstp' is an angular step by which to roll the 
               cutting half-planes about the target-illumination source
               vector, which we'll call the "axis." The ith half-plane is 
               rotated from `refvec' about the axis in the counter-clockwise 
               direction by i*rolstp. Units are radians. `rolstp' should 
               be set to  
 
                  2*pi/ncuts  
 
               to generate an approximately uniform distribution of 
               terminator points along the terminator. 
 
               `ncuts' is the number of cutting half-planes used to 
               find terminator points; the angular positions of 
               consecutive half-planes increase in the positive 
               sense (counterclockwise) about the axis and are 
               distributed roughly equally about that vector: each 
               half-plane has angular separation of approximately 
 
                  `rolstp' radians 
 
               from each of its neighbors. When the aberration 
               correction locus is set to "CENTER", the angular 
               separation is the value above, up to round-off. When 
               the locus is "ELLIPSOID TERMINATOR", the separations are 
               less uniform due to differences in the aberration 
               corrections used for the respective terminator points. 
 
 
   schstp, 
   soltol      are used only for DSK-based surfaces. These inputs
               are, respectively, the search angular step size and 
               solution convergence tolerance used to find tangent 
               rays and associated terminator points within each cutting 
               half plane. These values are used when the `method' 
               argument includes the "TANGENT" option. In this case, 
               terminator points are found by a two-step search process: 

                  1) Bracketing: starting with a direction having
                     sufficiently small angular separation from the
                     the axis, rays emanating from the surface of the 
                     illumination source are generated within the 
                     half-plane at successively greater angular separations 
                     from the axis, where the increment of angular separation 
                     is `schstp'. The rays are tested for intersection 
                     with the target surface. When a transition from
                     non-intersection to intersection is found, the
                     angular separation of a tangent ray has been
                     bracketed.

                  2) Root finding: each time a tangent ray is 
                     bracketed, a search is done to find the angular
                     separation from the axis at which a tangent ray 
                     exists. The search terminates when successive rays 
                     are separated by no more than `soltol'. When the 
                     search converges, the last ray-surface intersection 
                     point found in the convergence process is considered
                     to be a terminator point.
                       
               `schstp' and `soltol' have units of radians.

                Target bodies with simple surfaces---for example,
                convex shapes---will have a single terminator point
                within each cutting half-plane. For such surfaces,
                `schstp' can be set large enough so that only one
                bracketing step is taken. A value greater than pi,
                for example 4.0, is recommended.

                Target bodies with complex surfaces can have
                multiple terminator points within a given cutting
                half-plane. To find all terminator points, `schstp'
                must be set to a value smaller than the minimum angular
                separation of any two terminator points in any
                cutting half-plane, where the vertex of the angle is
                on the illumination source. `schstp' must not be too 
                small, or the search will be excessively slow.

                For both kinds of surfaces, `soltol' must be chosen so
                that the results will have the desired precision.
                Note that the choice of `soltol' required to meet a
                specified bound on terminator point height errors
                depends on the illumination source-target distance.


   maxn         is the maximum number of terminator points that can
                be stored in the output array.
</pre>


   <h2> Output </h2>

<pre>
                The returned {@link TerminatorPoint} array contains
                the terminator points found by this routine. The set of 
                terminator points associated with the ith half-plane 
                is contained in the ith row of the returned array. 
                The rows need not have equal length. 

                The terminator points in a given half-plane are ordered by 
                decreasing angular separation from the illumination 
                source-target direction; the outermost terminator 
                point in a given half-plane is the first of that set. 
 
                The terminator points for the half-plane containing `refvec' 
                occupy the first row of the output array 
 
                Terminator points are expressed in the reference frame 
                designated by `fixref'. For each terminator point, the 
                orientation of the frame is evaluated at the epoch 
                corresponding to the terminator point; see the description
                of the input argument `corloc' above for details.
</pre>


<h2> Method `create' Particulars </h2>

<p><b> Syntax of the `method' input argument   </b>
     
<p> The keywords and surface list in the `method' argument 
    of `create' are called "clauses." The clauses may appear in any 
    order, for example:
<pre> 
   UMBRAL/TANGENT/DSK/UNPRIORITIZED/&#60surface list&#62
   DSK/UMBRAL/TANGENT/&#60surface list&#62/UNPRIORITIZED 
   UNPRIORITIZED/&#60surface list&#62/DSK/TANGENT/UMBRAL
</pre> 
      The simplest form of the `method' argument specifying use of 
      DSK data is one that lacks a surface list, for example: 
<pre>
   "PENUMBRAL/TANGENT/DSK/UNPRIORITIZED" 
   "UMBRAL/GUIDED/DSK/UNPRIORITIZED" 
</pre>
   For applications in which all loaded DSK data for the target 
   body are for a single surface, and there are no competing 
   segments, the above strings suffice. This is expected to be 
   the usual case. 
<p>
   When, for the specified target body, there are loaded DSK 
   files providing data for multiple surfaces for that body, the 
   surfaces to be used by this routine for a given call must be 
   specified in a surface list, unless data from all of the 
   surfaces are to be used together. 
<p>
   The surface list consists of the string 
<pre>
      SURFACES = 
</pre>
   followed by a comma-separated list of one or more surface 
   identifiers. The identifiers may be names or integer codes in 
   string format. For example, suppose we have the surface 
   names and corresponding ID codes shown below: 
<pre> 
   Surface Name                              ID code 
   ------------                              ------- 
   "Mars MEGDR 128 PIXEL/DEG"                1 
   "Mars MEGDR 64 PIXEL/DEG"                 2 
   "Mars_MRO_HIRISE"                         3 
</pre>
   If data for all of the above surfaces are loaded, then 
   data for surface 1 can be specified by either 
<pre>
   "SURFACES = 1" 
</pre>
      or 
<pre>
   "SURFACES = \"Mars MEGDR 128 PIXEL/DEG\"" 
</pre>
      Double quotes are used to delimit the surface name because 
      it contains blank characters.  
<p>    
      To use data for surfaces 2 and 3 together, any 
      of the following surface lists could be used: 
<pre>
   "SURFACES = 2, 3" 
 
   "SURFACES = \"Mars MEGDR  64 PIXEL/DEG\", 3" 
 
   "SURFACES = 2, Mars_MRO_HIRISE" 
 
   "SURFACES = \"Mars MEGDR 64 PIXEL/DEG\", Mars_MRO_HIRISE" 
</pre>   
      An example of a `method' argument that could be constructed 
      using one of the surface lists above is 
<pre>
   "UMBRAL/TANGENT/DSK/UNPRIORITIZED/SURFACES= \"Mars MEGDR 64 PIXEL/DEG\",3" 
</pre>



   <h3>Code Examples</h3>

<p> 
   The numerical results shown for these examples may differ across 
   platforms. The results depend on the SPICE kernels used as 
   input, the compiler and supporting libraries, and the machine  
   specific arithmetic implementation.  
 
 
   <ol>    

   <li> 
      Find apparent terminator points on Phobos as seen from Mars.
      Use the "umbral" shadow definition. 

 
<p>   Due to Phobos' irregular shape, the TANGENT terminator point 
      definition will used. It suffices to compute light time and 
      stellar aberration corrections for the center of Phobos, so 
      the "CENTER" aberration correction locus will be used. Use 
      converged Newtonian light time and stellar aberration 
      corrections in order to model the apparent position and  
      orientation of Phobos. 
       
<p>   For comparison, compute terminator points using both ellipsoid 
      and topographic shape models. 
 
<p>   Use the target body-fixed +Z axis as the reference direction 
      for generating cutting half-planes. This choice enables the 
      user to see whether the first terminator point is near the target's 
      north pole. 
 
<p>   For each option, use just three cutting half-planes, in order 
      to keep the volume of output manageable. In most applications, 
      the number of cuts and the number of resulting terminator points 
      would be much greater. 
 
 
   </li>

<p>  Use the meta-kernel shown below to load the required SPICE
     kernels.

<pre>
KPL/MK

File: TerminatorPointEx1.tm

This meta-kernel is intended to support operation of SPICE
example programs. The kernels shown here should not be
assumed to contain adequate or correct versions of data
required by SPICE-based user applications.

In order for an application to use this meta-kernel, the
kernels referenced here must be present in the user's
current working directory.

The names and contents of the kernels referenced
by this meta-kernel are as follows:

  File name                        Contents
  ---------                        --------
  de430.bsp                        Planetary ephemeris
  mar097.bsp                       Mars satellite ephemeris
  pck00010.tpc                     Planet orientation and
                                   radii
  naif0012.tls                     Leapseconds
  phobos512.bds                    DSK based on
                                   Gaskell ICQ Q=512
                                   Phobos plate model
\begindata

  PATH_SYMBOLS    = 'GEN'
  PATH_VALUES     = '/ftp/pub/naif/generic_kernels'

  KERNELS_TO_LOAD = ( 'de430.bsp',
                      'mar097.bsp',
                      'pck00010.tpc',
                      'naif0012.tls',
                      '$GEN/dsk/phobos/phobos512.bds' )
\begintext

</pre>


<p> Example code begins here.

<pre>

//
// Program TerminatorPointEx1
//

import spice.basic.*;
import static spice.basic.AngularUnits.*;
import static java.lang.Math.PI;

//
// Find apparent terminator points on Phobos as seen from Mars. 
//
// Compute terminator points using the tangent definition, using the "umbral" 
// shadow type. The sun is the illumination source. Perform aberration 
// corrections for the target center. Use both ellipsoid and DSK shape models.
//
public class TerminatorPointEx1
{
   //
   // Load SPICE shared library.
   //
   static{ System.loadLibrary( "JNISpice" ); }


   public static void main( String[] args )

      throws SpiceException
   {
      //
      // Local constants
      //
      final String                      META   = "TerminatorPointEx1.tm";

      final int                         MAXN   = 10000;
      final int                         NMETH  = 2;

      //
      // Local variables
      //
      AberrationCorrection              abcorr = 
                                           new AberrationCorrection( "CN+S" );

      Body                              ilusrc = new Body( "SUN"    );
      Body                              obsrvr = new Body( "MARS"   );
      Body                              target = new Body( "PHOBOS" );
      
      PositionVector                    srcvec;

      TerminatorPoint[][]               terminatorPoints;

      ReferenceFrame                    J2000 =
                                           new ReferenceFrame( "J2000" );

      ReferenceFrame                    fixref =
                                           new ReferenceFrame( "IAU_PHOBOS" );

      String[]                          methds = 
                                        {
                                           "UMBRAL/TANGENT/ELLIPSOID",
                                           "UMBRAL/TANGENT/DSK/UNPRIORITIZED"
                                        };

      String                            corloc   = "CENTER";
      String                            utc      = "2008 AUG 11 00:00:00 UTC";

      TDBTime                           et;
      TDBTime                           trgepc;

      Vector3                           z = new Vector3( 0.0, 0.0, 1.0 );

      double                            delrol;  
      double                            dist;
      double[]                          pointArray;
      double                            roll;
      double                            schstp;
      double                            soltol;

      int                               i;
      int                               j;
      int                               k;
      int                               ncuts;
      int                               npts;


      try
      {
         //
         // Load kernels.
         //
         KernelDatabase.load( META );

         //
         // Convert the UTC request time to ET (seconds past
         // J2000, TDB). 
         //
         et = new TDBTime( utc );

         //
         // Compute a set of terminator points using light time and
         // stellar aberration corrections. Use both ellipsoid
         // and DSK shape models. Use an angular step size corresponding
         // to a height of about 100 meters to ensure we don't miss the
         // terminator. Set the convergence tolerance to limit
         // the height convergence error to about 1 meter.
         // Compute 3 terminator points for each computation method.
         //
         // Get the approximate light source-target distance
         // at `et'. We'll ignore the observer-target light
         // time for this approximation.
         //

         srcvec = new PositionVector ( ilusrc, et,    J2000,  
                                       abcorr, target       );
         dist   = srcvec.norm();

         schstp = 1.0e-1 / dist;
         soltol = 1.0e-3 / dist;
         ncuts  = 3;

         System.out.format ( "%n"                   +
                             "Light source:   %s%n" +
                             "Observer:       %s%n" +
                             "Target:         %s%n" +
                             "Frame:          %s%n" +
                             "%n"                   +
                             "Number of cuts: %d%n",
                             ilusrc.getName(),
                             obsrvr.getName(),
                             target.getName(),
                             fixref.getName(),
                             ncuts                  );

         delrol = 2*PI / ncuts;

         for ( i = 0;  i < NMETH;  i++ )
         {
            //
            // Compute a set of terminator points using the current
            // computation method.
            //
            terminatorPoints = 

               TerminatorPoint.create( methds[i], ilusrc, target, et, 
                                       fixref,    abcorr, corloc, obsrvr,
                                       z,         delrol, ncuts,  schstp, 
                                       soltol,    MAXN                   );
            //
            // Write the results.
            //
            System.out.format ( "%n%n"                      +
                                "Computation method = %s%n" +
                                "Locus              = %s%n", 
                                methds[i],
                                corloc                       );

            for ( j = 0;  j < ncuts;  j++ )
            {
               //
               // Display the roll angle, target epoch, and terminator point
               // count for the current cutting half-plane. Note that
               // the epoch associated with the first terminator point applies
               // to all points in the current half-plane.
               //
               roll   = j * delrol;
               npts   = terminatorPoints[j].length;
               trgepc = terminatorPoints[j][0].getTargetEpoch();

               System.out.format ( "%n"                                +
                                   "  Roll angle (deg) = %21.9f%n"     +
                                   "     Target epoch  = %21.9f%n"     +
                                   "     Number of terminator points " + 
                                   "at this roll angle: %d%n",
                                   roll * DPR,
                                   trgepc.getTDBSeconds(),
                                   npts                                    );

               System.out.format ( "      Terminator points%n" );

               for ( k = 0;  k < npts;  k++ )
               {
                  pointArray = terminatorPoints[j][k].toArray();

                  System.out.format ( " %20.9f %20.9f %20.9f%n",
                                      pointArray[0],
                                      pointArray[1],
                                      pointArray[2]               );

               } // End of loop for current cut.

            } // End of loop for terminator, using current method.
         
         } // End of method loop.
         
         System.out.format ( "%n" );

      } // End of try block

      catch ( SpiceException exc )
      {
         exc.printStackTrace();
      }

   } // End of main method 
   
}


</pre>

<p> When this program was executed on a PC/Linux/gcc/64-bit/java 1.5
platform, the output was:

<pre>

Light source:   SUN
Observer:       MARS
Target:         PHOBOS
Frame:          IAU_PHOBOS

Number of cuts: 3


Computation method = UMBRAL/TANGENT/ELLIPSOID
Locus              = CENTER

  Roll angle (deg) =           0.000000000
     Target epoch  =   271684865.152078200
     Number of terminator points at this roll angle: 1
      Terminator points
          2.040498332          5.012722925          8.047281838

  Roll angle (deg) =         120.000000000
     Target epoch  =   271684865.152078200
     Number of terminator points at this roll angle: 1
      Terminator points
        -11.058054707          0.167672089         -4.782740292

  Roll angle (deg) =         240.000000000
     Target epoch  =   271684865.152078200
     Number of terminator points at this roll angle: 1
      Terminator points
          8.195238564         -6.093889437         -5.122310498


Computation method = UMBRAL/TANGENT/DSK/UNPRIORITIZED
Locus              = CENTER

  Roll angle (deg) =           0.000000000
     Target epoch  =   271684865.152078200
     Number of terminator points at this roll angle: 1
      Terminator points
          1.626396122          3.995432317          8.853689531

  Roll angle (deg) =         120.000000000
     Target epoch  =   271684865.152078200
     Number of terminator points at this roll angle: 1
      Terminator points
        -11.186659739         -0.142366278         -4.646137201

  Roll angle (deg) =         240.000000000
     Target epoch  =   271684865.152078200
     Number of terminator points at this roll angle: 1
      Terminator points
          9.338447077         -6.091352469         -5.960849305

</pre>

 
   </ol>

   */
   public static TerminatorPoint[][] create ( String                  method,
                                              Body                    ilusrc,
                                              Body                    target,
                                              Time                    et,
                                              ReferenceFrame          fixref,
                                              AberrationCorrection    abcorr,
                                              String                  corloc,
                                              Body                    obsrvr,
                                              Vector3                 refvec,
                                              double                  rolstp,
                                              int                     ncuts,
                                              double                  schstp,
                                              double                  soltol,
                                              int                     maxn    )
      throws SpiceException

   {
      //
      // Allocate space for the output arrays returned by 
      //
      //    CSPICE.termpt
      //
      int[]                             npts   = new int   [ ncuts ];
      double[][]                        points = new double[ maxn ][3];
      double[]                          epochs = new double[ maxn ];
      double[][]                        tangts = new double[ maxn ][3];

      //
      // Delegate the job to the CSPICE terminator finding method.
      //
      CSPICE.termpt ( method,             ilusrc.getName(), target.getName(),
                      et.getTDBSeconds(), fixref.getName(), abcorr.getName(), 
                      corloc,             obsrvr.getName(), refvec.toArray(), 
                      rolstp,             ncuts,            schstp,        
                      soltol,             maxn,             npts, 
                      points,             epochs,           tangts           );

      //
      // retArray is the TerminatorPoint array to be returned. The row 
      // dimensions are unknown as of yet.
      //
      TerminatorPoint[][] retArray = new TerminatorPoint[ ncuts ][];

      //
      // Set the values of the output terminator point array.
      //
      // `pix' is the "point index": it's the index of the current
      // terminator point in the `points' array.
      //
      int pix = 0;

      for ( int cut = 0;  cut < ncuts;  cut++ )
      {
         //
         // retArray[cut] is the row of TerminatorPoints in the current cutting
         // half-plane.
         //
         int rowsiz    = npts[cut];
        
         retArray[cut] = new TerminatorPoint[ rowsiz ];

         for ( int i = 0;  i < rowsiz;  i++ )
         {
            retArray[cut][i] = 

               new TerminatorPoint( new Vector3( points[pix] ),
                                    new TDBTime( epochs[pix] ), 
                                    new Vector3( tangts[pix] )  );
            ++pix;
         }
      }

      return ( retArray );
   }


 

   /**
   Return the target epoch from a TerminatorPoint instance. This
   method returns a deep copy.
   */
   public TDBTime getTargetEpoch()
   {
      return (  new TDBTime(targetEpoch)  );
   }

   /**
   Return the observer to terminator point vector from a TerminatorPoint 
   instance. This method returns a deep copy.
   */
   public Vector3 getSurfaceVector()
   {
      return (  new Vector3(surfaceVector)  );
   }

}
