<?php
	$host='localhost';
	$uname='root';
	$pwd='wlqrkrhtlvek';
	$db='project';

	$con = mysql_connect($host,$uname,$pwd) or die("connection failed");

	mysql_select_db($db,$con) or die("db selection failed");

	mysql_query("set names utf8");  


	$car = $_GET[PLATE];
	
	$result=mysql_query("SELECT * FROM kpuparking_entrance WHERE car_number_plate='$car'", $con);
	
	$arr=mysql_fetch_array($result);

	mysql_close($con);

	$now = date("Y-m-d H:i:s");
	$dbdata = $arr[1];

	$today = strtotime($now);
	$date = strtotime($dbdata);

	$diff = $today - $date;

	$hours = floor($diff/3600);
	$diff = $diff-($hours*3600);
	$min = floor($diff/60);
	$sec = $diff - ($min*60);

	$arr=array();
	$arr[0]['hour']=$hours;
	$arr[0]['minute']=$min;
	$arr[0]['second']=$sec;
	
	//$time="$hours : $min : $sec";

	header('Content-Type: application/json; charset=utf8');
	$json = json_encode(array("fee"=>$arr), JSON_PRETTY_PRINT+JSON_UNESCAPED_UNICODE);
	print($json);

?>