var robot = require("robotjs");
var screen = robot.getScreenSize();
setTimeout(handleChrome,2000);

function handleChrome() {
    robot.moveMouseSmooth(32,349);
    robot.mouseClick();
    robot.moveMouseSmooth(469,78);
    robot.mouseClick;
    robot.typeString("www.gmail.com")
    robot.keyTap("enter");
}
