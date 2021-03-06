/**
 * Copyright (c) 2011, The University of Southampton and the individual contributors.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *   * 	Redistributions of source code must retain the above copyright notice,
 * 	this list of conditions and the following disclaimer.
 *
 *   *	Redistributions in binary form must reproduce the above copyright notice,
 * 	this list of conditions and the following disclaimer in the documentation
 * 	and/or other materials provided with the distribution.
 *
 *   *	Neither the name of the University of Southampton nor the names of its
 * 	contributors may be used to endorse or promote products derived from this
 * 	software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.openimaj.image.feature.local.detector.dog.pyramid;

import org.openimaj.image.FImage;
import org.openimaj.image.MBFImage;
import org.openimaj.image.analysis.pyramid.gaussian.GaussianOctave;
import org.openimaj.image.analysis.pyramid.gaussian.GaussianPyramid;
import org.openimaj.image.analysis.pyramid.gaussian.GaussianPyramidOptions;
import org.openimaj.image.feature.local.detector.pyramid.OctaveInterestPointFinder;
import org.openimaj.image.feature.local.detector.pyramid.OctaveInterestPointListener;


/**
 * A {@link FirstBandDoGOctaveExtremaFinder} is an {@link OctaveInterestPointFinder} that
 * can be applied to {@link GaussianOctave}s. A {@link FirstBandDoGOctaveExtremaFinder} 
 * works like a {@link DoGOctaveExtremaFinder}, but with an {@link MBFImage}, however, 
 * only the first band of the {@link MBFImage} is used to build the DoG pyramid and for
 * the application of the inner finder. 
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public class FirstBandDoGOctaveExtremaFinder 
	implements 
		OctaveInterestPointFinder<GaussianOctave<MBFImage>, MBFImage>, 
		OctaveInterestPointListener<GaussianOctave<FImage>, FImage> 
{
	GaussianOctave<MBFImage> gaussianOctave; //the Gaussian octave
	FirstBandDoGOctave dogOctave;	//a difference-of-Gaussian octave constructed from the Gaussian one
	OctaveInterestPointFinder<GaussianOctave<FImage>, FImage> innerFinder; //the finder that is applied to the DoG
	OctaveInterestPointListener<GaussianOctave<MBFImage>, MBFImage> listener; //a listener that is fired as interest points are detected
	
	/**
	 * Construct with the given finder.
	 * @param finder the finder
	 */
	public FirstBandDoGOctaveExtremaFinder(OctaveInterestPointFinder<GaussianOctave<FImage>, FImage> finder) {
		this.innerFinder = finder;
		
		finder.setOctaveInterestPointListener(this);
	}
	
	/**
	 * Construct with the given finder and listener.
	 * @param finder the finder
	 * @param listener the listener
	 */
	public FirstBandDoGOctaveExtremaFinder(OctaveInterestPointFinder<GaussianOctave<FImage>, FImage> finder, OctaveInterestPointListener<GaussianOctave<MBFImage>, MBFImage> listener) {
		this(finder);
		this.listener = listener;
	}

	@Override
	public void setOctaveInterestPointListener(OctaveInterestPointListener<GaussianOctave<MBFImage>, MBFImage> listener) {
		this.listener = listener;
	}
	
	@Override
	public OctaveInterestPointListener<GaussianOctave<MBFImage>, MBFImage> getOctaveInterestPointListener() {
		return listener;
	}
	
	@Override
	public void process(GaussianOctave<MBFImage> octave) {
		gaussianOctave = octave;
		
		GaussianPyramidOptions<FImage> opts = new GaussianPyramidOptions<FImage>(octave.options);
		GaussianPyramid<FImage> gp = new GaussianPyramid<FImage>(opts);
		
		dogOctave = new FirstBandDoGOctave(gp, octave.octaveSize);
		dogOctave.process(octave);
		
		innerFinder.process(dogOctave);
	}

	@Override
	public GaussianOctave<MBFImage> getOctave() {
		return gaussianOctave;
	}
	
	/**
	 * Get the difference-of-Gaussian octave corresponding to
	 * the current Gaussian octave.
	 * @return the difference-of-Gaussian octave
	 */
	public GaussianOctave<FImage> getDoGOctave() {
		return dogOctave;
	}

	@Override
	public int getCurrentScaleIndex() {
		return innerFinder.getCurrentScaleIndex();
	}

	@Override
	public void foundInterestPoint(OctaveInterestPointFinder<GaussianOctave<FImage>, FImage> finder, float x, float y, float octaveScale) {
		if (listener != null) listener.foundInterestPoint(this, x, y, octaveScale);
	}
}
