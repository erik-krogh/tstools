(function () {
	/**
	 * @author bhouston / http://exocortex.com
	 */

	var x = 2;
	var y = 3;
	var z = 4;
	var w = 5;

	var negInf2 = new THREE.Vector2( -Infinity, -Infinity );
	var posInf2 = new THREE.Vector2( Infinity, Infinity );

	var zero2 = new THREE.Vector2();
	var one2 = new THREE.Vector2( 1, 1 );
	var two2 = new THREE.Vector2( 2, 2 );

	var negInf3 = new THREE.Vector3( -Infinity, -Infinity, -Infinity );
	var posInf3 = new THREE.Vector3( Infinity, Infinity, Infinity );

	var zero3 = new THREE.Vector3();
	var one3 = new THREE.Vector3( 1, 1, 1 );
	var two3 = new THREE.Vector3( 2, 2, 2 );


	/**
	 * @author bhouston / http://exocortex.com
	 */

	module( "Triangle" );

	test( "constructor", function() {
		var a = new THREE.Triangle();
		ok( a.a.equals( zero3 ), "Passed!" );
		ok( a.b.equals( zero3 ), "Passed!" );
		ok( a.c.equals( zero3 ), "Passed!" );

		a = new THREE.Triangle( one3.clone().negate(), one3.clone(), two3.clone() );
		ok( a.a.equals( one3.clone().negate() ), "Passed!" );
		ok( a.b.equals( one3 ), "Passed!" );
		ok( a.c.equals( two3 ), "Passed!" );
	});

	test( "copy", function() {
		var a = new THREE.Triangle( one3.clone().negate(), one3.clone(), two3.clone() );
		var b = new THREE.Triangle().copy( a );
		ok( b.a.equals( one3.clone().negate() ), "Passed!" );
		ok( b.b.equals( one3 ), "Passed!" );
		ok( b.c.equals( two3 ), "Passed!" );

		// ensure that it is a true copy
		a.a = one3;
		a.b = zero3;
		a.c = zero3;
		ok( b.a.equals( one3.clone().negate() ), "Passed!" );
		ok( b.b.equals( one3 ), "Passed!" );
		ok( b.c.equals( two3 ), "Passed!" );
	});

	test( "setFromPointsAndIndices", function() {
		var a = new THREE.Triangle();

		var points = [ one3, one3.clone().negate(), two3 ];
		a.setFromPointsAndIndices( points, 1, 0, 2 );
		ok( a.a.equals( one3.clone().negate() ), "Passed!" );
		ok( a.b.equals( one3 ), "Passed!" );
		ok( a.c.equals( two3 ), "Passed!" );

	});

	test( "set", function() {
		var a = new THREE.Triangle();

		a.set( one3.clone().negate(), one3, two3 );
		ok( a.a.equals( one3.clone().negate() ), "Passed!" );
		ok( a.b.equals( one3 ), "Passed!" );
		ok( a.c.equals( two3 ), "Passed!" );

	});

	test( "area", function() {
		var a = new THREE.Triangle();

		ok( a.area() == 0, "Passed!" );

		a = new THREE.Triangle( new THREE.Vector3( 0, 0, 0 ), new THREE.Vector3( 1, 0, 0 ), new THREE.Vector3( 0, 1, 0 ) );
		ok( a.area() == 0.5, "Passed!" );

		a = new THREE.Triangle( new THREE.Vector3( 2, 0, 0 ), new THREE.Vector3( 0, 0, 0 ), new THREE.Vector3( 0, 0, 2 ) );
		ok( a.area() == 2, "Passed!" );

		// colinear triangle.
		a = new THREE.Triangle( new THREE.Vector3( 2, 0, 0 ), new THREE.Vector3( 0, 0, 0 ), new THREE.Vector3( 3, 0, 0 ) );
		ok( a.area() == 0, "Passed!" );
	});

	test( "midpoint", function() {
		var a = new THREE.Triangle();

		ok( a.midpoint().equals( new THREE.Vector3( 0, 0, 0 ) ), "Passed!" );

		a = new THREE.Triangle( new THREE.Vector3( 0, 0, 0 ), new THREE.Vector3( 1, 0, 0 ), new THREE.Vector3( 0, 1, 0 ) );
		ok( a.midpoint().equals( new THREE.Vector3( 1/3, 1/3, 0 ) ), "Passed!" );

		a = new THREE.Triangle( new THREE.Vector3( 2, 0, 0 ), new THREE.Vector3( 0, 0, 0 ), new THREE.Vector3( 0, 0, 2 ) );
		ok( a.midpoint().equals( new THREE.Vector3( 2/3, 0, 2/3 ) ), "Passed!" );
	});

	test( "normal", function() {
		var a = new THREE.Triangle();

		ok( a.normal().equals( new THREE.Vector3( 0, 0, 0 ) ), "Passed!" );

		a = new THREE.Triangle( new THREE.Vector3( 0, 0, 0 ), new THREE.Vector3( 1, 0, 0 ), new THREE.Vector3( 0, 1, 0 ) );
		ok( a.normal().equals( new THREE.Vector3( 0, 0, 1 ) ), "Passed!" );

		a = new THREE.Triangle( new THREE.Vector3( 2, 0, 0 ), new THREE.Vector3( 0, 0, 0 ), new THREE.Vector3( 0, 0, 2 ) );
		ok( a.normal().equals( new THREE.Vector3( 0, 1, 0 ) ), "Passed!" );
	});

	test( "plane", function() {
		var a = new THREE.Triangle();

		// artificial normal is created in this case.
		ok( a.plane().distanceToPoint( a.a ) == 0, "Passed!" );
		ok( a.plane().distanceToPoint( a.b ) == 0, "Passed!" );
		ok( a.plane().distanceToPoint( a.c ) == 0, "Passed!" );
		ok( a.plane().normal.equals( a.normal() ), "Passed!" );

		a = new THREE.Triangle( new THREE.Vector3( 0, 0, 0 ), new THREE.Vector3( 1, 0, 0 ), new THREE.Vector3( 0, 1, 0 ) );
		ok( a.plane().distanceToPoint( a.a ) == 0, "Passed!" );
		ok( a.plane().distanceToPoint( a.b ) == 0, "Passed!" );
		ok( a.plane().distanceToPoint( a.c ) == 0, "Passed!" );
		ok( a.plane().normal.equals( a.normal() ), "Passed!" );

		a = new THREE.Triangle( new THREE.Vector3( 2, 0, 0 ), new THREE.Vector3( 0, 0, 0 ), new THREE.Vector3( 0, 0, 2 ) );
		ok( a.plane().distanceToPoint( a.a ) == 0, "Passed!" );
		ok( a.plane().distanceToPoint( a.b ) == 0, "Passed!" );
		ok( a.plane().distanceToPoint( a.c ) == 0, "Passed!" );
		ok( a.plane().normal.clone().normalize().equals( a.normal() ), "Passed!" );
	});

	test( "barycoordFromPoint", function() {
		var a = new THREE.Triangle();

		var bad = new THREE.Vector3( -2, -1, -1 );

		ok( a.barycoordFromPoint( a.a ).equals( bad ), "Passed!" );
		ok( a.barycoordFromPoint( a.b ).equals( bad ), "Passed!" );
		ok( a.barycoordFromPoint( a.c ).equals( bad ), "Passed!" );

		a = new THREE.Triangle( new THREE.Vector3( 0, 0, 0 ), new THREE.Vector3( 1, 0, 0 ), new THREE.Vector3( 0, 1, 0 ) );
		ok( a.barycoordFromPoint( a.a ).equals( new THREE.Vector3( 1, 0, 0 ) ), "Passed!" );
		ok( a.barycoordFromPoint( a.b ).equals( new THREE.Vector3( 0, 1, 0 ) ), "Passed!" );
		ok( a.barycoordFromPoint( a.c ).equals( new THREE.Vector3( 0, 0, 1 ) ), "Passed!" );
		ok( a.barycoordFromPoint( a.midpoint() ).distanceTo( new THREE.Vector3( 1/3, 1/3, 1/3 ) ) < 0.0001, "Passed!" );

		a = new THREE.Triangle( new THREE.Vector3( 2, 0, 0 ), new THREE.Vector3( 0, 0, 0 ), new THREE.Vector3( 0, 0, 2 ) );
		ok( a.barycoordFromPoint( a.a ).equals( new THREE.Vector3( 1, 0, 0 ) ), "Passed!" );
		ok( a.barycoordFromPoint( a.b ).equals( new THREE.Vector3( 0, 1, 0 ) ), "Passed!" );
		ok( a.barycoordFromPoint( a.c ).equals( new THREE.Vector3( 0, 0, 1 ) ), "Passed!" );
		ok( a.barycoordFromPoint( a.midpoint() ).distanceTo( new THREE.Vector3( 1/3, 1/3, 1/3 ) ) < 0.0001, "Passed!" );
	});

	test( "containsPoint", function() {
		var a = new THREE.Triangle();

		ok( ! a.containsPoint( a.a ), "Passed!" );
		ok( ! a.containsPoint( a.b ), "Passed!" );
		ok( ! a.containsPoint( a.c ), "Passed!" );

		a = new THREE.Triangle( new THREE.Vector3( 0, 0, 0 ), new THREE.Vector3( 1, 0, 0 ), new THREE.Vector3( 0, 1, 0 ) );
		ok( a.containsPoint( a.a ), "Passed!" );
		ok( a.containsPoint( a.b ), "Passed!" );
		ok( a.containsPoint( a.c ), "Passed!" );
		ok( a.containsPoint( a.midpoint() ), "Passed!" );
		ok( ! a.containsPoint( new THREE.Vector3( -1, -1, -1 ) ), "Passed!" );

		a = new THREE.Triangle( new THREE.Vector3( 2, 0, 0 ), new THREE.Vector3( 0, 0, 0 ), new THREE.Vector3( 0, 0, 2 ) );
		ok( a.containsPoint( a.a ), "Passed!" );
		ok( a.containsPoint( a.b ), "Passed!" );
		ok( a.containsPoint( a.c ), "Passed!" );
		ok( a.containsPoint( a.midpoint() ), "Passed!" );
		ok( ! a.containsPoint( new THREE.Vector3( -1, -1, -1 ) ), "Passed!" );
	});

})();