component extends="org.lucee.cfml.test.LuceeTestCase" labels="toon" {

	function run( testResults, testBox ) {

		describe( "TOON basic serialization", function() {

			it( "serializes a simple struct", function() {
				var data = { name: "Zac", age: 42 };
				var result = SerializeTOON( data );
				expect( result ).toBeString();
				expect( result ).toInclude( "Zac" );
				expect( result ).toInclude( "42" );
			});

			it( "serializes an array", function() {
				var data = [ 1, 2, 3, 4, 5 ];
				var result = SerializeTOON( data );
				expect( result ).toBeString();
			});

			it( "serializes nested structures", function() {
				var data = {
					user: {
						name: "Zac",
						address: {
							city: "Sydney",
							country: "Australia"
						}
					}
				};
				var result = SerializeTOON( data );
				expect( result ).toBeString();
				expect( result ).toInclude( "Sydney" );
			});

			it( "serializes with prettyPrint option", function() {
				var data = { name: "Zac", age: 42 };
				var result = SerializeTOON( data, { prettyPrint: true } );
				expect( result ).toBeString();
			});

			it( "serializes with cycleSupport disabled", function() {
				var data = { name: "Zac", age: 42 };
				var result = SerializeTOON( data, { cycleSupport: false } );
				expect( result ).toBeString();
			});

		});

	}

}
