/*
 * Copyright (c) 2009 The Jackson Laboratory
 * 
 * This software was developed by Gary Churchill's Lab at The Jackson
 * Laboratory (see http://research.jax.org/faculty/churchill).
 *
 * This is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this software.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.jax.qtl.scan;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

/**
 * For filtering out the results.
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public abstract class ScanResultFilter
{
    private final PropertyChangeSupport propertyChangeSupport =
        new PropertyChangeSupport(this);
    
    /**
     * Absolute Confidence filter enum values
     */
    public enum AbsoluteConfidenceFilter
    {
        /**
         * don't do any absolute confidence filtering
         */
        NO_ABSOLUTE_CONFIDENCE_FILTER
        {
            /**
             * {@inheritDoc}
             */
            @Override
            public String toString()
            {
                return "No Absolute Filtering";
            }
        },
        
        /**
         * filter on absolute lod score
         */
        LOD_SCORE_FILTER
        {
            /**
             * {@inheritDoc}
             */
            @Override
            public String toString()
            {
                return "Minimum LOD Score";
            }
        },
        
        /**
         * filter on absolute alpha-value
         */
        ALPHA_VALUE_FILTER
        {
            /**
             * {@inheritDoc}
             */
            @Override
            public String toString()
            {
                return "Alpha Value (Maximum p-value)";
            }
        };
    }
    
    private volatile AbsoluteConfidenceFilter absoluteConfidenceFilterState =
        AbsoluteConfidenceFilter.NO_ABSOLUTE_CONFIDENCE_FILTER;
    
    private final Map<AbsoluteConfidenceFilter, Double> absoluteConfidenceFilterValues;
    
    /**
     * Marker relative enum values
     */
    public enum MarkerRelativeConfidenceFilter
    {
        /**
         * Don't do any relative filtering
         */
        NO_MARKER_RELATIVE_CONFIDENCE_FILTER
        {
            /**
             * {@inheritDoc}
             */
            @Override
            public String toString()
            {
                return "No Marker Relative Filtering";
            }
        },
        
        /**
         * keep only local LOD maxima within a selected spacing
         */
        LOCAL_MAXIMA_WITH_MINIMUM_SPACING_FILTER
        {
            /**
             * {@inheritDoc}
             */
            @Override
            public String toString()
            {
                return "Show Local Maxima With Minimum Marker Spacing (cM)";
            }
        },
        
        /**
         * keep only chromosome-wide LOD maxima
         */
        CHROMOSOME_MAXIMA_FILTER
        {
            /**
             * {@inheritDoc}
             */
            @Override
            public String toString()
            {
                return "Show Chromosome Maxima";
            }
        };
    }
    
    private volatile MarkerRelativeConfidenceFilter markerRelativeConfidenceFilterState =
        MarkerRelativeConfidenceFilter.NO_MARKER_RELATIVE_CONFIDENCE_FILTER;
    
    private final Map<MarkerRelativeConfidenceFilter, Double> markerRelativeConfidenceFilterValues;
    
    /**
     * Constructor
     */
    public ScanResultFilter()
    {
        this(null, null);
    }
    
    /**
     * Constructor
     * @param initialAbsoluteConfidenceFilterValues
     *          the initial values for absolute confidence
     * @param initialMarkerRelativeConfidenceFilterValues
     *          the initial values for marker-relative confidence
     */
    public ScanResultFilter(
            Map<AbsoluteConfidenceFilter, Double> initialAbsoluteConfidenceFilterValues,
            Map<MarkerRelativeConfidenceFilter, Double> initialMarkerRelativeConfidenceFilterValues)
    {
        if(initialAbsoluteConfidenceFilterValues == null)
        {
            this.absoluteConfidenceFilterValues = Collections.synchronizedMap(
                    new EnumMap<AbsoluteConfidenceFilter, Double>(
                            AbsoluteConfidenceFilter.class));
        }
        else
        {
            this.absoluteConfidenceFilterValues = Collections.synchronizedMap(
                    new EnumMap<AbsoluteConfidenceFilter, Double>(
                            initialAbsoluteConfidenceFilterValues));
        }
        
        if(initialMarkerRelativeConfidenceFilterValues == null)
        {
            this.markerRelativeConfidenceFilterValues = Collections.synchronizedMap(
                    new EnumMap<MarkerRelativeConfidenceFilter, Double>(
                            MarkerRelativeConfidenceFilter.class));
        }
        else
        {
            this.markerRelativeConfidenceFilterValues = Collections.synchronizedMap(
                    new EnumMap<MarkerRelativeConfidenceFilter, Double>(
                            initialMarkerRelativeConfidenceFilterValues));
        }
    }
    
    /**
     * Add a property change listener
     * @param listener
     *          the listener to add
     */
    public void addPropertyChangeListener(PropertyChangeListener listener)
    {
        this.propertyChangeSupport.addPropertyChangeListener(listener);
    }
    
    /**
     * Remove a property change listener
     * @param listener
     *          the listener to remove
     */
    public void removePropertyChangeListener(PropertyChangeListener listener)
    {
        this.propertyChangeSupport.removePropertyChangeListener(listener);
    }
    
    /**
     * Getter for the confidence filter state
     * @return the absoluteConfidenceFilterState
     */
    public AbsoluteConfidenceFilter getAbsoluteConfidenceFilterState()
    {
        return this.absoluteConfidenceFilterState;
    }
    
    /**
     * the property name used for the confidence filter state when we
     * fire property change events
     */
    public static final String ABSOLUTE_CONFIDENCE_FILTER_STATE_PROPERTY_NAME =
        "absoluteConfidenceFilterState";
    
    /**
     * Setter for the confidence filter state
     * @param absoluteConfidenceFilterState the absoluteConfidenceFilterState to set
     */
    public void setAbsoluteConfidenceFilterState(AbsoluteConfidenceFilter absoluteConfidenceFilterState)
    {
        AbsoluteConfidenceFilter oldConfidenceFilterState = this.absoluteConfidenceFilterState;
        this.absoluteConfidenceFilterState = absoluteConfidenceFilterState;
        this.propertyChangeSupport.firePropertyChange(
                ABSOLUTE_CONFIDENCE_FILTER_STATE_PROPERTY_NAME,
                oldConfidenceFilterState,
                absoluteConfidenceFilterState);
    }
    
    /**
     * Getter for the confidence filter's value
     * @return the confidenceFilterValue
     */
    public double getAbsoluteConfidenceFilterValue()
    {
        AbsoluteConfidenceFilter absoluteConfidenceFilterState =
            this.absoluteConfidenceFilterState;
        if(absoluteConfidenceFilterState == null)
        {
            return 0.0;
        }
        else
        {
            Double absoluteConfidenceValue =
                this.absoluteConfidenceFilterValues.get(absoluteConfidenceFilterState);
            return absoluteConfidenceValue == null ?
                   0.0 :
                   absoluteConfidenceValue.doubleValue();
        }
    }
    
    /**
     * the property name used for the confidence filter value when we
     * fire property change events
     */
    public static final String ABSOLUTE_CONFIDENCE_FILTER_VALUE_PROPERTY_NAME =
        "confidenceFilterValue";
    
    /**
     * Setter for the confidence filter's value
     * @param absoluteConfidenceFilterValue the confidenceFilterValue to set
     */
    public void setAbsoluteConfidenceFilterValue(double absoluteConfidenceFilterValue)
    {
        double oldConfidenceFilterValue = this.getAbsoluteConfidenceFilterValue();
        AbsoluteConfidenceFilter absoluteConfidenceFilterState =
            this.absoluteConfidenceFilterState;
        if(absoluteConfidenceFilterState != null)
        {
            this.absoluteConfidenceFilterValues.put(
                    absoluteConfidenceFilterState,
                    absoluteConfidenceFilterValue);
        }
        
        this.propertyChangeSupport.firePropertyChange(
                ABSOLUTE_CONFIDENCE_FILTER_VALUE_PROPERTY_NAME,
                oldConfidenceFilterValue,
                absoluteConfidenceFilterValue);
    }
    
    /**
     * Getter for the marker relative filter state
     * @return the markerRelativeFilterState
     */
    public MarkerRelativeConfidenceFilter getMarkerRelativeConfidenceFilterState()
    {
        return this.markerRelativeConfidenceFilterState;
    }
    
    /**
     * the property name used for the marker relative state when we
     * fire property change events
     */
    public static final String MARKER_RELATIVE_CONFIDENCE_FILTER_STATE_PROPERTY_NAME =
        "markerRelativeFilterState";
    
    /**
     * Setter for the marker relative filter state
     * @param markerRelativeConfidenceFilterState the markerRelativeFilterState to set
     */
    public void setMarkerRelativeConfidenceFilterState(
            MarkerRelativeConfidenceFilter markerRelativeConfidenceFilterState)
    {
        MarkerRelativeConfidenceFilter oldMarkerRelativeFilterState =
            this.markerRelativeConfidenceFilterState;
        this.markerRelativeConfidenceFilterState = markerRelativeConfidenceFilterState;
        this.propertyChangeSupport.firePropertyChange(
                MARKER_RELATIVE_CONFIDENCE_FILTER_STATE_PROPERTY_NAME,
                oldMarkerRelativeFilterState,
                markerRelativeConfidenceFilterState);
    }
    
    /**
     * Getter for the marker relative filter value
     * @return the markerRelativeFilterValue
     */
    public double getMarkerRelativeConfidenceFilterValue()
    {
        MarkerRelativeConfidenceFilter markerRelativeConfidenceFilter =
            this.markerRelativeConfidenceFilterState;
        if(markerRelativeConfidenceFilter == null)
        {
            return 0.0;
        }
        else
        {
            Double markerRelativeConfidenceValue =
                this.markerRelativeConfidenceFilterValues.get(
                        markerRelativeConfidenceFilter);
            return markerRelativeConfidenceValue == null ?
                   0.0 :
                   markerRelativeConfidenceValue.doubleValue();
        }
    }
    
    /**
     * the property name used for the marker relative value when we
     * fire property change events
     */
    public static final String MARKER_RELATIVE_CONFIDENCE_FILTER_VALUE_PROPERTY_NAME =
        "markerRelativeFilterValue";
    
    /**
     * Setter for the marker relative filter value
     * @param markerRelativeConfidenceFilterValue the markerRelativeFilterValue to set
     */
    public void setMarkerRelativeConfidenceFilterValue(double markerRelativeConfidenceFilterValue)
    {
        double oldMarkerRelativeFilterValue =
            this.getMarkerRelativeConfidenceFilterValue();
        MarkerRelativeConfidenceFilter markerRelativeConfidenceFilterState =
            this.markerRelativeConfidenceFilterState;
        if(markerRelativeConfidenceFilterState != null)
        {
            this.markerRelativeConfidenceFilterValues.put(
                    markerRelativeConfidenceFilterState,
                    markerRelativeConfidenceFilterValue);
        }
        this.propertyChangeSupport.firePropertyChange(
                MARKER_RELATIVE_CONFIDENCE_FILTER_VALUE_PROPERTY_NAME,
                oldMarkerRelativeFilterValue,
                markerRelativeConfidenceFilterValue);
    }
}
