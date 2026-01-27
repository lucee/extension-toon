component extends="org.lucee.cfml.test.LuceeTestCase" labels="toon" {

	function run( testResults, testBox ) {

		describe( "TOON circular reference handling", function() {

			// SKIPPED: json-io ToonWriter does not detect cycles in Map/Collection types
			// See: https://github.com/jdereg/json-io - bug in ToonWriter.java

			xit( "handles simple circular reference", function() {
				var parent = { name: "Parent" };
				var child = { name: "Child", parent: parent };
				parent.child = child;

				var toon = SerializeTOON( parent );
				expect( toon ).toBeString();

				var restored = DeserializeTOON( toon );
				expect( restored.name ).toBe( "Parent" );
				expect( restored.child.name ).toBe( "Child" );
			});

			xit( "handles self-reference", function() {
				var obj = { name: "Self" };
				obj.self = obj;

				var toon = SerializeTOON( obj );
				expect( toon ).toBeString();

				var restored = DeserializeTOON( toon );
				expect( restored.name ).toBe( "Self" );
			});

			xit( "handles array with circular reference", function() {
				var items = [];
				var item1 = { id: 1, related: items };
				var item2 = { id: 2, related: items };
				arrayAppend( items, item1 );
				arrayAppend( items, item2 );

				var toon = SerializeTOON( items );
				expect( toon ).toBeString();
			});

		});

	}

}
