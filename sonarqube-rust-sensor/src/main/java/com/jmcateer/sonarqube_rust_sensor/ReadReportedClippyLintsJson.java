package com.jmcateer.sonarqube_rust_sensor;

import java.util.List;
import java.util.Objects;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;

import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONTokener;

public class ReadReportedClippyLintsJson {

    private static final Logger LOGGER = Loggers.get(ReadReportedClippyLintsJson.class);

    public static void ReadReported(String clippy_output_json, ReadReportedClippyLintsJsonFunction consumer_func) {

        LOGGER.warn("TODO ReadReported {}", clippy_output_json);

        int num_open_braces = 0;
        int last_open_brace_i = 0;
        int num_close_braces = 0;
        int last_close_brace_i = 0;

        int num_quote_chars_seen = 0; // Only processbraces on even numbers; yeah yeah recursive escape-processing can be done l8ter.

        for (int i=0; i<clippy_output_json.length(); i+=1) {
            if (clippy_output_json.charAt(i) == '"') {
                num_quote_chars_seen += 1;
            }
            if (num_quote_chars_seen % 2 == 0) { // If we are NOT "within" a string
                if (num_open_braces == num_close_braces && clippy_output_json.charAt(i) == '{') {
                    num_open_braces += 1;
                    last_open_brace_i = i;
                }
                else if (clippy_output_json.charAt(i) == '}') {
                    num_close_braces += 1;
                    last_close_brace_i = i;
                    // Now parse last JSON IF brace counts match
                    if (num_open_braces == num_close_braces) {
                        // Parse it!
                        try {
                            String json_fragment = clippy_output_json.substring(last_open_brace_i, last_close_brace_i+1);
                            JSONTokener tokener = new JSONTokener(json_fragment);
                            JSONObject json = new JSONObject(tokener);

                            // Json may look like;
                            //    {"reason":"compiler-message","package_id":"path+file:///j/proj/sonarqube-rust-sensor/example_rust_code#0.1.0","manifest_path":"/j/proj/sonarqube-rust-sensor/example_rust_code/Cargo.toml","target":{"kind":["bin"],"crate_types":["bin"],"name":"example_rust_code","src_path":"/j/proj/sonarqube-rust-sensor/example_rust_code/src/main.rs","edition":"2024","doc":true,"doctest":false,"test":true},"message":{"rendered":"warning: function `foo` is never used\n  --> src/main.rs:21:10\n   |\n21 | async fn foo() {}\n   |          ^^^\n   |\n   = note: `#[warn(dead_code)]` on by default\n\n","$message_type":"diagnostic","children":[{"children":[],"code":null,"level":"note","message":"`#[warn(dead_code)]` on by default","rendered":null,"spans":[]}],"code":{"code":"dead_code","explanation":null},"level":"warning","message":"function `foo` is never used","spans":[{"byte_end":762,"byte_start":759,"column_end":13,"column_start":10,"expansion":null,"file_name":"src/main.rs","is_primary":true,"label":null,"line_end":21,"line_start":21,"suggested_replacement":null,"suggestion_applicability":null,"text":[{"highlight_end":13,"highlight_start":10,"text":"async fn foo() {}"}]}]}}
                            //    {"reason":"compiler-artifact","package_id":"registry+https://github.com/rust-lang/crates.io-index#ref-cast@1.0.24","manifest_path":"/j/.cargo/registry/src/index.crates.io-1949cf8c6b5b557f/ref-cast-1.0.24/Cargo.toml","target":{"kind":["lib"],"crate_types":["lib"],"name":"ref_cast","src_path":"/j/.cargo/registry/src/index.crates.io-1949cf8c6b5b557f/ref-cast-1.0.24/src/lib.rs","edition":"2021","doc":true,"doctest":true,"test":true},"profile":{"opt_level":"0","debuginfo":2,"debug_assertions":true,"overflow_checks":true,"test":false},"features":[],"filenames":["/j/proj/sonarqube-rust-sensor/example_rust_code/target/debug/deps/libref_cast-972d113da6d04a90.rmeta"],"executable":null,"fresh":true}
                            //    {"reason":"compiler-message","package_id":"path+file:///j/proj/sonarqube-rust-sensor/example_rust_code#0.1.0","manifest_path":"/j/proj/sonarqube-rust-sensor/example_rust_code/Cargo.toml","target":{"kind":["bin"],"crate_types":["bin"],"name":"example_rust_code","src_path":"/j/proj/sonarqube-rust-sensor/example_rust_code/src/main.rs","edition":"2024","doc":true,"doctest":false,"test":true},"message":{"rendered":"error: an async construct yields a type which is itself awaitable\n  --> src/main.rs:25:5\n   |\n24 |     let x = async {\n   |  _________________-\n25 | |     foo()\n   | |     ^^^^^\n   | |     |\n   | |     awaitable value not awaited\n   | |     help: consider awaiting this value: `foo().await`\n26 | |   };\n   | |___- outer async construct\n   |\n   = help: for further information visit https://rust-lang.github.io/rust-clippy/master/index.html#async_yields_async\n   = note: `#[deny(clippy::async_yields_async)]` on by default\n\n","$message_type":"diagnostic","children":[{"children":[],"code":null,"level":"help","message":"for further information visit https://rust-lang.github.io/rust-clippy/master/index.html#async_yields_async","rendered":null,"spans":[]},{"children":[],"code":null,"level":"note","message":"`#[deny(clippy::async_yields_async)]` on by default","rendered":null,"spans":[]},{"children":[],"code":null,"level":"help","message":"consider awaiting this value","rendered":null,"spans":[{"byte_end":807,"byte_start":802,"column_end":10,"column_start":5,"expansion":null,"file_name":"src/main.rs","is_primary":true,"label":null,"line_end":25,"line_start":25,"suggested_replacement":"foo().await","suggestion_applicability":"MaybeIncorrect","text":[{"highlight_end":10,"highlight_start":5,"text":"    foo()"}]}]}],"code":{"code":"clippy::async_yields_async","explanation":null},"level":"error","message":"an async construct yields a type which is itself awaitable","spans":[{"byte_end":811,"byte_start":796,"column_end":4,"column_start":17,"expansion":null,"file_name":"src/main.rs","is_primary":false,"label":"outer async construct","line_end":26,"line_start":24,"suggested_replacement":null,"suggestion_applicability":null,"text":[{"highlight_end":18,"highlight_start":17,"text":"  let x = async {"},{"highlight_end":10,"highlight_start":1,"text":"    foo()"},{"highlight_end":4,"highlight_start":1,"text":"  };"}]},{"byte_end":807,"byte_start":802,"column_end":10,"column_start":5,"expansion":null,"file_name":"src/main.rs","is_primary":true,"label":"awaitable value not awaited","line_end":25,"line_start":25,"suggested_replacement":null,"suggestion_applicability":null,"text":[{"highlight_end":10,"highlight_start":5,"text":"    foo()"}]}]}}

                            String reason = json.getString("reason");
                            if (reason != null && reason.equals("compiler-message")) {
                                // Clippy lints appear as compile-message documents
                                JSONObject message = json.getJSONObject("message");
                                JSONArray msg_children = message.getJSONArray("children");

                                String level = message.getString("level"); // "warning", "error", whatevs
                                String simple_msg = message.getString("message");
                                String rendered_msg = message.getString("rendered");



                            }

                        }
                        catch (Throwable e) {
                          LOGGER.warn("Unexpected exception in ReadReportedClippyLintsJson::ReadReported  {}", e);
                        }
                    }
                }
            }
        }

        /*
        InputStreamReader streamReader = new InputStreamReader(clippy_output_json, java.nio.charset.StandardCharsets.UTF_8);
        BufferedReader reader = new BufferedReader(streamReader);

        JSONTokener tokener = new JSONTokener(reader);
        JSONArray json = new JSONArray(tokener);

        LOGGER.warn("Read json={}", json);

        for (int i=0; i<json.length(); i+=1) {
            try {
                JSONObject rule_o = json.getJSONObject(i);
                String rule_id = rule_o.getString("rule_id");
                String rule_name = rule_o.getString("rule_name");
                String rule_description_html = rule_o.getString("rule_description_html");
                String lint_group = rule_o.getString("lint_group");
                String lint_level = rule_o.getString("lint_level");

                consumer_func.accept(rule_id, rule_name, rule_description_html, lint_group, lint_level);
            }
            catch (Throwable e) {
              LOGGER.warn("Unexpected exception in ReadAllClippyRules::WithAllRules reading json[{}] {}", i, e);
            }
        }
        */

    }

}
