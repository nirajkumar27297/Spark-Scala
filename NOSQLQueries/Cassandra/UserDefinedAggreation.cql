
CREATE OR REPLACE FUNCTION mode_state (state map<ascii,int>, goals_for int, goals_against int)
CALLED ON NULL INPUT RETURNS map<ascii,int> LANGUAGE java AS '	
	String key = new String (goals_for + " - " + goals_against);
	state.put(key, 1 + state.getOrDefault(key, 0));
	return state;
';
CREATE OR REPLACE FUNCTION mode_final (state map<ascii,int>)
CALLED ON NULL INPUT RETURNS ascii LANGUAGE JAVA AS '
	String mostCommon = null;
	int max_count = 0;
	for (String key : state.keySet()) {
	int value = state.get(key);
	if (value > max_count) { mostCommon = key;max_count = value; }}
	return mostCommon;
';
CREATE AGGREGATE IF NOT EXISTS mode (int, int) 
  SFUNC mode_state
  STYPE map<ascii,int>
  FINALFUNC mode_final 
  INITCOND {};

SELECT mode(goals_for,goals_against) FROM games_by_team WHERE league_id='English Premier League' AND season='2015/16';
