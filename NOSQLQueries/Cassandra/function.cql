create or replace function margin (goals_for int,goals_against int)
         returns null on null input
         returns int language java as '
         return Integer.valueOf(Math.abs(goals_for - goals_against));';
         
SELECT margin(goals_for, goals_against) FROM games_by_team
WHERE league_id='English Premier League' AND season='2015/16' and team = 'Tottenham Hotspur';

select avg(margin(goals_for,goals_against)) from games_by_team WHERE league_id='English Premier League' AND season='2015/16' AND team = 'Manchester United';
select max(margin(goals_for, goals_against)) from games_by_team WHERE league_id='English Premier League' AND season='2015/16';
