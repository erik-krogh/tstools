///////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2001, Eric D. Friedman All Rights Reserved.
// Copyright (c) 2009, Rob Eden All Rights Reserved.
// Copyright (c) 2009, Jeff Randall All Rights Reserved.
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
///////////////////////////////////////////////////////////////////////////////

package gnu.trove.set.hash;

import gnu.trove.set.TFloatSet;
import gnu.trove.iterator.TFloatIterator;
import gnu.trove.impl.*;
import gnu.trove.impl.hash.*;
import gnu.trove.TFloatCollection;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Externalizable;
import java.util.Arrays;
import java.util.Collection;


//////////////////////////////////////////////////
// THIS IS A GENERATED CLASS. DO NOT HAND EDIT! //
//////////////////////////////////////////////////


/**
 * An open addressed set implementation for float primitives.
 *
 * @author Eric D. Friedman
 * @author Rob Eden
 * @author Jeff Randall
 */

public class TFloatHashSet extends TFloatHash implements TFloatSet, Externalizable {
	static final long serialVersionUID = 1L;


    /**
     * Creates a new <code>TFloatHashSet</code> instance with the default
     * capacity and load factor.
     */
    public TFloatHashSet() {
        super();
    }


    /**
     * Creates a new <code>TFloatHashSet</code> instance with a prime
     * capacity equal to or greater than <tt>initialCapacity</tt> and
     * with the default load factor.
     *
     * @param initialCapacity an <code>int</code> value
     */
    public TFloatHashSet( int initialCapacity ) {
        super( initialCapacity );
    }


    /**
     * Creates a new <code>TIntHash</code> instance with a prime
     * value at or near the specified capacity and load factor.
     *
     * @param initialCapacity used to find a prime capacity for the table.
     * @param load_factor used to calculate the threshold over which
     * rehashing takes place.
     */
    public TFloatHashSet( int initialCapacity, float load_factor ) {
        super( initialCapacity, load_factor );
    }


    /**
     * Creates a new <code>TFloatHashSet</code> instance with a prime
     * capacity equal to or greater than <tt>initial_capacity</tt> and
     * with the specified load factor.
     *
     * @param initial_capacity an <code>int</code> value
     * @param load_factor a <code>float</code> value
     * @param no_entry_value a <code>float</code> value that represents null.
     */
    public TFloatHashSet( int initial_capacity, float load_factor,
            float no_entry_value ) {
        super( initial_capacity, load_factor, no_entry_value );
        //noinspection RedundantCast
        if ( no_entry_value != ( float ) 0 ) {
            Arrays.fill( _set, no_entry_value );
        }
    }


    /**
      * Creates a new <code>TFloatHashSet</code> instance that is a copy
      * of the existing Collection.
      *
      * @param collection a <tt>Collection</tt> that will be duplicated.
      */
     public TFloatHashSet( Collection<? extends Float> collection ) {
        this( Math.max( collection.size(), DEFAULT_CAPACITY ) );
        addAll( collection );
     }


    /**
     * Creates a new <code>TFloatHashSet</code> instance that is a copy
     * of the existing set.
     *
     * @param collection a <tt>TFloatSet</tt> that will be duplicated.
     */
    public TFloatHashSet( TFloatCollection collection ) {
        this( Math.max( collection.size(), DEFAULT_CAPACITY ) );
        if ( collection instanceof TFloatHashSet ) {
            TFloatHashSet hashset = ( TFloatHashSet ) collection;
            this._loadFactor = hashset._loadFactor;
            this.no_entry_value = hashset.no_entry_value;
            //noinspection RedundantCast
            if ( this.no_entry_value != ( float ) 0 ) {
                Arrays.fill( _set, this.no_entry_value );
            }
            setUp( (int) Math.ceil( DEFAULT_CAPACITY / _loadFactor ) );
        }
        addAll( collection );
    }


    /**
     * Creates a new <code>TFloatHashSet</code> instance containing the
     * elements of <tt>array</tt>.
     *
     * @param array an array of <code>float</code> primitives
     */
    public TFloatHashSet( float[] array ) {
        this( Math.max( array.length, DEFAULT_CAPACITY ) );
        addAll( array );
    }


    /** {@inheritDoc} */
    public TFloatIterator iterator() {
        return new TFloatHashIterator( this );
    }


    /** {@inheritDoc} */
    public float[] toArray() {
        float[] result = new float[ size() ];
        if ( result.length == 0 ) {
            return result;      // nothing to copy
        }
        float[] set = _set;
        byte[] states = _states;

        for ( int i = states.length, j = 0; i-- > 0; ) {
            if ( states[i] == FULL ) {
                result[j++] = set[i];
            }
        }
        return result;
    }


    /** {@inheritDoc} */
    public float[] toArray( float[] dest ) {
        if ( dest.length == 0 ) {
            return dest;        // nothing to copy
        }
        float[] set = _set;
        byte[] states = _states;

        for ( int i = states.length, j = 0; i-- > 0; ) {
            if ( states[i] == FULL ) {
                dest[j++] = set[i];
            }
        }

        if ( dest.length > _size ) {
            dest[_size] = no_entry_value;
        }
        return dest;
    }


    /** {@inheritDoc} */
    public boolean add( float val ) {
        int index = insertKey(val);

        if ( index < 0 ) {
            return false;       // already present in set, nothing to add
        }

        postInsertHook( consumeFreeSlot );

        return true;            // yes, we added something
    }


    /** {@inheritDoc} */
    public boolean remove( float val ) {
        int index = index(val);
        if ( index >= 0 ) {
            removeAt( index );
            return true;
        }
        return false;
    }


    /** {@inheritDoc} */
    public boolean containsAll( Collection<?> collection ) {
        for ( Object element : collection ) {
            if ( element instanceof Float ) {
                float c = ( ( Float ) element ).floatValue();
                if ( ! contains( c ) ) {
                    return false;
                }
            } else {
                return false;
            }

        }
        return true;
    }


    /** {@inheritDoc} */
    public boolean containsAll( TFloatCollection collection ) {
        TFloatIterator iter = collection.iterator();
        while ( iter.hasNext() ) {
            float element = iter.next();
            if ( ! contains( element ) ) {
                return false;
            }
        }
        return true;
    }


    /** {@inheritDoc} */
    public boolean containsAll( float[] array ) {
        for ( int i = array.length; i-- > 0; ) {
            if ( ! contains( array[i] ) ) {
                return false;
            }
        }
        return true;
    }


    /** {@inheritDoc} */
    public boolean addAll( Collection<? extends Float> collection ) {
        boolean changed = false;
        for ( Float element : collection ) {
            float e = element.floatValue();
            if ( add( e ) ) {
                changed = true;
            }
        }
        return changed;
    }


    /** {@inheritDoc} */
    public boolean addAll( TFloatCollection collection ) {
        boolean changed = false;
        TFloatIterator iter = collection.iterator();
        while ( iter.hasNext() ) {
            float element = iter.next();
            if ( add( element ) ) {
                changed = true;
            }
        }
        return changed;
    }


    /** {@inheritDoc} */
    public boolean addAll( float[] array ) {
        boolean changed = false;
        for ( int i = array.length; i-- > 0; ) {
            if ( add( array[i] ) ) {
                changed = true;
            }
        }
        return changed;
    }


    /** {@inheritDoc} */
    @SuppressWarnings({"SuspiciousMethodCalls"})
    public boolean retainAll( Collection<?> collection ) {
        boolean modified = false;
	    TFloatIterator iter = iterator();
	    while ( iter.hasNext() ) {
	        if ( ! collection.contains( Float.valueOf ( iter.next() ) ) ) {
		        iter.remove();
		        modified = true;
	        }
	    }
	    return modified;
    }


    /** {@inheritDoc} */
    public boolean retainAll( TFloatCollection collection ) {
        if ( this == collection ) {
            return false;
        }
        boolean modified = false;
	    TFloatIterator iter = iterator();
	    while ( iter.hasNext() ) {
	        if ( ! collection.contains( iter.next() ) ) {
		        iter.remove();
		        modified = true;
	        }
	    }
	    return modified;
    }


    /** {@inheritDoc} */
    public boolean retainAll( float[] array ) {
        boolean changed = false;
        Arrays.sort( array );
        float[] set = _set;
        byte[] states = _states;

        _autoCompactTemporaryDisable = true;
        for ( int i = set.length; i-- > 0; ) {
            if ( states[i] == FULL && ( Arrays.binarySearch( array, set[i] ) < 0) ) {
                removeAt( i );
                changed = true;
            }
        }
        _autoCompactTemporaryDisable = false;

        return changed;
    }


    /** {@inheritDoc} */
    public boolean removeAll( Collection<?> collection ) {
        boolean changed = false;
        for ( Object element : collection ) {
            if ( element instanceof Float ) {
                float c = ( ( Float ) element ).floatValue();
                if ( remove( c ) ) {
                    changed = true;
                }
            }
        }
        return changed;
    }


    /** {@inheritDoc} */
    public boolean removeAll( TFloatCollection collection ) {
        boolean changed = false;
        TFloatIterator iter = collection.iterator();
        while ( iter.hasNext() ) {
            float element = iter.next();
            if ( remove( element ) ) {
                changed = true;
            }
        }
        return changed;
    }


    /** {@inheritDoc} */
    public boolean removeAll( float[] array ) {
        boolean changed = false;
        for ( int i = array.length; i-- > 0; ) {
            if ( remove(array[i]) ) {
                changed = true;
            }
        }
        return changed;
    }


    /** {@inheritDoc} */
    public void clear() {
        super.clear();
        float[] set = _set;
        byte[] states = _states;

        for ( int i = set.length; i-- > 0; ) {
            set[i] = no_entry_value;
            states[i] = FREE;
        }
    }


    /** {@inheritDoc} */
    protected void rehash( int newCapacity ) {
        int oldCapacity = _set.length;
        
        float oldSet[] = _set;
        byte oldStates[] = _states;

        _set = new float[newCapacity];
        _states = new byte[newCapacity];

        for ( int i = oldCapacity; i-- > 0; ) {
            if( oldStates[i] == FULL ) {
                float o = oldSet[i];
                int index = insertKey(o);
            }
        }
    }


    /** {@inheritDoc} */
    public boolean equals( Object other ) {
        if ( ! ( other instanceof TFloatSet ) ) {
            return false;
        }
        TFloatSet that = ( TFloatSet ) other;
        if ( that.size() != this.size() ) {
            return false;
        }
        for ( int i = _states.length; i-- > 0; ) {
            if ( _states[i] == FULL ) {
                if ( ! that.contains( _set[i] ) ) {
                    return false;
                }
            }
        }
        return true;
    }


    /** {@inheritDoc} */
    public int hashCode() {
        int hashcode = 0;
        for ( int i = _states.length; i-- > 0; ) {
            if ( _states[i] == FULL ) {
                hashcode += HashFunctions.hash( _set[i] );
            }
        }
        return hashcode;
    }


    /** {@inheritDoc} */
    public String toString() {
        StringBuilder buffy = new StringBuilder( _size * 2 + 2 );
        buffy.append("{");
        for ( int i = _states.length, j = 1; i-- > 0; ) {
            if ( _states[i] == FULL ) {
                buffy.append( _set[i] );
                if ( j++ < _size ) {
                    buffy.append( "," );
                }
            }
        }
        buffy.append("}");
        return buffy.toString();
    }


    class TFloatHashIterator extends THashPrimitiveIterator implements TFloatIterator {

        /** the collection on which the iterator operates */
        private final TFloatHash _hash;

        /** {@inheritDoc} */
        public TFloatHashIterator( TFloatHash hash ) {
            super( hash );
            this._hash = hash;
        }

        /** {@inheritDoc} */
        public float next() {
            moveToNextIndex();
            return _hash._set[_index];
        }
    }


    /** {@inheritDoc} */
    public void writeExternal( ObjectOutput out ) throws IOException {

    	// VERSION
    	out.writeByte( 1 );

    	// SUPER
    	super.writeExternal( out );

    	// NUMBER OF ENTRIES
    	out.writeInt( _size );

        // LOAD FACTOR -- Added version 1
        out.writeFloat( _loadFactor );

        // NO ENTRY VALUE -- Added version 1
        out.writeFloat( no_entry_value );

    	// ENTRIES
        for ( int i = _states.length; i-- > 0; ) {
            if ( _states[i] == FULL ) {
                out.writeFloat( _set[i] );
            }
        }
    }


    /** {@inheritDoc} */
    public void readExternal( ObjectInput in )
    	throws IOException, ClassNotFoundException {

    	// VERSION
    	int version = in.readByte();

        // SUPER
    	super.readExternal( in );

    	// NUMBER OF ENTRIES
        int size = in.readInt();

        if ( version >= 1 ) {
            // LOAD FACTOR
            _loadFactor = in.readFloat();

            // NO ENTRY VALUE
            no_entry_value = in.readFloat();
            //noinspection RedundantCast
            if ( no_entry_value != ( float ) 0 ) {
                Arrays.fill( _set, no_entry_value );
            }
        }

    	// ENTRIES
        setUp( size );
        while ( size-- > 0 ) {
            float val = in.readFloat();
            add( val );
        }
    }
} // TIntHashSet
