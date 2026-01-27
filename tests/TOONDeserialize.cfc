component extends="org.lucee.cfml.test.LuceeTestCase" labels="toon" {

	function run( testResults, testBox ) {

		describe( "TOON deserialization", function() {

			it( "deserializes a simple struct", function() {
				var data = { name: "Zac", age: 42 };
				var toon = SerializeTOON( data );
				var result = DeserializeTOON( toon );
				expect( result ).toBeStruct();
				expect( result.name ).toBe( "Zac" );
				expect( result.age ).toBe( 42 );
			});

			it( "deserializes an array", function() {
				var data = [ 1, 2, 3, 4, 5 ];
				var toon = SerializeTOON( data );
				var result = DeserializeTOON( toon );
				expect( result ).toBeArray();
				expect( arrayLen( result ) ).toBe( 5 );
				expect( result[ 1 ] ).toBe( 1 );
				expect( result[ 5 ] ).toBe( 5 );
			});

			it( "deserializes nested structures", function() {
				var data = {
					user: {
						name: "Zac",
						address: {
							city: "Sydney",
							country: "Australia"
						}
					}
				};
				var toon = SerializeTOON( data );
				var result = DeserializeTOON( toon );
				expect( result ).toBeStruct();
				expect( result.user.name ).toBe( "Zac" );
				expect( result.user.address.city ).toBe( "Sydney" );
			});

			it( "deserializes boolean values", function() {
				var data = { active: true, deleted: false };
				var toon = SerializeTOON( data );
				var result = DeserializeTOON( toon );
				expect( result.active ).toBeTrue();
				expect( result.deleted ).toBeFalse();
			});

			it( "deserializes null as empty string", function() {
				var data = { value: javacast( "null", "" ) };
				var toon = SerializeTOON( data );
				var result = DeserializeTOON( toon );
				expect( result ).toBeStruct();
			});

		});

	}

}
