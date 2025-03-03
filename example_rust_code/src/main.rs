fn main() {
    let input_json_string = std::env::args().nth(1).unwrap_or( std::env::var("INPUT_JSON_STRING").unwrap_or(r#"{"TODO":"Set arg1", "TODO2": "Or env var INPUT_JSON_STRING", "TODO3": "To read in those json dictionaries instead of this boring dribble.", "A-Number": 123}"#.to_string()) );
    println!("input_json_string={input_json_string}");

    match sonic_rs::from_str::<sonic_rs::Value>(&input_json_string) {
        Ok(parsed_values) => {
            println!("parsed_values={parsed_values}");
        }
        Err(e) => {
            println!("Error parsing JSON: {:?}", e);
        }
    }

}

#[cfg(test)]
mod our_tests {
    #[test]
    fn test_parse_empty_dict() {
        assert_eq!(
            format!("{:?}", sonic_rs::from_str::<sonic_rs::Value>("{}") ),
            format!("{:?}", Ok::<sonic_rs::Value, sonic_rs::Error>(sonic_rs::json!({})) )
        );
    }

    #[test]
    fn test_parse_empty_list() {
        assert_eq!(
            format!("{:?}", sonic_rs::from_str::<sonic_rs::Value>("[]") ),
            format!("{:?}", Ok::<sonic_rs::Value, sonic_rs::Error>(sonic_rs::json!([])) )
        );
    }
}


