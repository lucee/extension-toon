component extends="org.lucee.cfml.test.LuceeTestCase" labels="toon" {

	function run( testResults, testBox ) {

		describe( "TOON round-trip serialization", function() {

			it( "round-trips a complex structure", function() {
				var original = {
					id: 12345,
					name: "Test Product",
					price: 99.99,
					tags: [ "electronics", "gadgets", "sale" ],
					specs: {
						weight: 1.5,
						dimensions: { width: 10, height: 5, depth: 2 }
					},
					inStock: true,
					discontinued: false
				};

				var toon = SerializeTOON( original );
				var restored = DeserializeTOON( toon );

				expect( restored.id ).toBe( original.id );
				expect( restored.name ).toBe( original.name );
				expect( restored.price ).toBe( original.price );
				expect( arrayLen( restored.tags ) ).toBe( 3 );
				expect( restored.specs.weight ).toBe( 1.5 );
				expect( restored.specs.dimensions.width ).toBe( 10 );
				expect( restored.inStock ).toBeTrue();
				expect( restored.discontinued ).toBeFalse();
			});

			it( "round-trips an array of structs", function() {
				var original = [
					{ id: 1, name: "Item 1" },
					{ id: 2, name: "Item 2" },
					{ id: 3, name: "Item 3" }
				];

				var toon = SerializeTOON( original );
				var restored = DeserializeTOON( toon );

				expect( arrayLen( restored ) ).toBe( 3 );
				expect( restored[ 1 ].id ).toBe( 1 );
				expect( restored[ 2 ].name ).toBe( "Item 2" );
				expect( restored[ 3 ].id ).toBe( 3 );
			});

			it( "round-trips special characters in strings", function() {
				var original = {
					message: "Hello ""World""!",
					path: "C:\Users\test",
					unicode: "日本語テスト"
				};

				var toon = SerializeTOON( original );
				var restored = DeserializeTOON( toon );

				expect( restored.message ).toBe( original.message );
				expect( restored.unicode ).toBe( original.unicode );
			});

			it( "round-trips with cycleSupport disabled", function() {
				var original = { name: "Simple", value: 42 };

				var toon = SerializeTOON( original, { cycleSupport: false } );
				var restored = DeserializeTOON( toon );

				expect( restored.name ).toBe( "Simple" );
				expect( restored.value ).toBe( 42 );
			});

		});

	}

}
