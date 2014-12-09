ADDRESS[0]="54.172.156.61"
ADDRESS[1]="54.170.194.191"
ADDRESS[2]="54.177.211.40"
ADDRESS[3]="54.255.79.0"
ADDRESS[4]="177.71.138.204"
PORT[0]="5321,2001"
PORT[1]="5322,2002"
PORT[2]="5323,2003"
PORT[3]="5324,2004"
PORT[4]="5325,2005"
KEY[0]="hollister.pem"
KEY[1]="ire_hollister.pem"
KEY[2]="cali_hollister.pem"
KEY[3]="sing_hollister.pem"
KEY[4]="south_hollister.pem"

rm ips
for i in 0 1 2 3 4
do
	echo "${ADDRESS[$i]},${PORT[$i]}" >> ips
done

for i in 0 1 2 3 4
do
	echo "${ADDRESS[$i]}"
	echo ubuntu@"${ADDRESS[$i]}":~/ips
	scp -o StrictHostKeyChecking=no -i ${KEY[$i]} ips ubuntu@${ADDRESS[$i]}:~/ips
	scp -o StrictHostKeyChecking=no -i ${KEY[$i]} atm.jar ubuntu@${ADDRESS[$i]}:~/atm.jar
	ssh -i ${KEY[$i]} ubuntu@${ADDRESS[$i]} "./atm.jar ips $i &"
done