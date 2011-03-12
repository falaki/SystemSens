/** 
  *
  * Copyright (c) 2011, The Regents of the University of California. All
  * rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are
  * met:
  *
  *   * Redistributions of source code must retain the above copyright
  *   * notice, this list of conditions and the following disclaimer.
  *
  *   * Redistributions in binary form must reproduce the above copyright
  *   * notice, this list of conditions and the following disclaimer in
  *   * the documentation and/or other materials provided with the
  *   * distribution.
  *
  *   * Neither the name of the University of California nor the names of
  *   * its contributors may be used to endorse or promote products
  *   * derived from this software without specific prior written
  *   * permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
  * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
  * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
  * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT
  * HOLDER> BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
  * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
  * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
  * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
  * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
  * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  *
  */

package edu.ucla.cens.systemsens;


interface IAdaptiveApplication
{

    /**
     * Returns the name of this application. 
     * E.g. WiFiGPSLocation
     *
     * @return      name of the application
     */
    String getName();

    /**
     * Returns the list of names of work units.
     * E.g. <"GPS", "WiFiScan">
     *
     * @return      names of work units.
     */
    List identifyList();



    /**
     * Return a vector of doubles indicating the amount of work done.
     * Each adaptive application should count its its major energy
     * consuming operations. Each time this method is called the
     * cumulative count of all the activities should be returned. 
     * The order should be consistant. The counts can be fractional 
     * values (double).
     * E.g. <126.0, 1763.0>
     *
     * @return      List of doubles indicating work units
     *
     */
    List getWork();

    /**
     * Sets the amount of work that the application is allowed
     * to perform. The order of this list is the same as that
     * retuend by getWork().
     *
     * @param       workLimit    List of allowed work units
     */
    void setWorkLimit(in List workLimit);


    /**
     * Sets the allowed rate of work. Each element of the list
     * indicates the number of work units that can be done during
     * each second.
     *
     * @param       workRate    List of allowed rates
    void setWorkRate(in List workRate);
     */

}
