///////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2008, Robert D. Eden All Rights Reserved.
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

package gnu.trove.impl.unmodifiable;


//////////////////////////////////////////////////
// THIS IS A GENERATED CLASS. DO NOT HAND EDIT! //
//////////////////////////////////////////////////

////////////////////////////////////////////////////////////
// THIS IS AN IMPLEMENTATION CLASS. DO NOT USE DIRECTLY!  //
// Access to these methods should be through TCollections //
////////////////////////////////////////////////////////////


import gnu.trove.iterator.*;
import gnu.trove.procedure.*;
import gnu.trove.set.*;
import gnu.trove.list.*;
import gnu.trove.function.*;
import gnu.trove.map.*;
import gnu.trove.*;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.Map;
import java.util.RandomAccess;
import java.util.Random;
import java.io.Serializable;
import java.io.ObjectOutputStream;
import java.io.IOException;


public class TUnmodifiableShortLongMap implements TShortLongMap, Serializable {
	private static final long serialVersionUID = -1034234728574286014L;

	private final TShortLongMap m;

	public TUnmodifiableShortLongMap( TShortLongMap m ) {
		if ( m == null )
			throw new NullPointerException();
		this.m = m;
	}

	public int size()                       { return m.size(); }
	public boolean isEmpty()                { return m.isEmpty(); }
	public boolean containsKey( short key )   { return m.containsKey( key ); }
	public boolean containsValue( long val ) { return m.containsValue( val ); }
	public long get( short key)                { return m.get( key ); }

	public long put( short key, long value ) { throw new UnsupportedOperationException(); }
	public long remove( short key ) { throw new UnsupportedOperationException(); }
	public void putAll( TShortLongMap m ) { throw new UnsupportedOperationException(); }
	public void putAll( Map<? extends Short, ? extends Long> map ) { throw new UnsupportedOperationException(); }
	public void clear() { throw new UnsupportedOperationException(); }

	private transient TShortSet keySet = null;
	private transient TLongCollection values = null;

	public TShortSet keySet() {
		if ( keySet == null )
			keySet = TCollections.unmodifiableSet( m.keySet() );
		return keySet;
	}
	public short[] keys() { return m.keys(); }
	public short[] keys( short[] array ) { return m.keys( array ); }

	public TLongCollection valueCollection() {
		if ( values == null )
			values = TCollections.unmodifiableCollection( m.valueCollection() );
		return values;
	}
	public long[] values() { return m.values(); }
	public long[] values( long[] array ) { return m.values( array ); }

	public boolean equals(Object o) { return o == this || m.equals(o); }
	public int hashCode()           { return m.hashCode(); }
	public String toString()        { return m.toString(); }
	public short getNoEntryKey()      { return m.getNoEntryKey(); }
	public long getNoEntryValue()    { return m.getNoEntryValue(); }

	public boolean forEachKey( TShortProcedure procedure ) {
		return m.forEachKey( procedure );
	}
	public boolean forEachValue( TLongProcedure procedure ) {
		return m.forEachValue( procedure );
	}
	public boolean forEachEntry( TShortLongProcedure procedure ) {
		return m.forEachEntry( procedure );
	}

	public TShortLongIterator iterator() {
		return new TShortLongIterator() {
			TShortLongIterator iter = m.iterator();

			public short key() { return iter.key(); }
			public long value() { return iter.value(); }
			public void advance() { iter.advance(); }
			public boolean hasNext() { return iter.hasNext(); }
			public long setValue( long val ) { throw new UnsupportedOperationException(); }
			public void remove() { throw new UnsupportedOperationException(); }
		};
	}

	public long putIfAbsent( short key, long value ) { throw new UnsupportedOperationException(); }
	public void transformValues( TLongFunction function ) { throw new UnsupportedOperationException(); }
	public boolean retainEntries( TShortLongProcedure procedure ) { throw new UnsupportedOperationException(); }
	public boolean increment( short key ) { throw new UnsupportedOperationException(); }
	public boolean adjustValue( short key, long amount ) { throw new UnsupportedOperationException(); }
	public long adjustOrPutValue( short key, long adjust_amount, long put_amount ) { throw new UnsupportedOperationException(); }
}
