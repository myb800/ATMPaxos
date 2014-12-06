ADDRESS[0]="54.172.156.61"
ADDRESS[1]="54.165.143.103"
ADDRESS[2]="54.172.151.170"
ADDRESS[3]="54.165.154.188"
ADDRESS[4]="54.165.199.218"
PORT[0]="1001,2001"
PORT[1]="1002,2002"
PORT[2]="1003,2003"
PORT[3]="1004,2004"
PORT[4]="1005,2005"

rm ips
for i in 0 1 2 3 4
do
	echo "${ADDRESS[$i]},${PORT[$i]}" >> ips
done

for i in 0 1 2 3 4
do
	echo "${ADDRESS[$i]}"
	echo ubuntu@"${ADDRESS[$i]}":~/ips
	scp -o StrictHostKeyChecking=no -i hollister.pem ips ubuntu@${ADDRESS[$i]}:~/ips
	scp -o StrictHostKeyChecking=no -i hollister.pem atm.jar ubuntu@${ADDRESS[$i]}:~/atm.jar
	ssh -i hollister.pem ubuntu@${ADDRESS[$i]} "./atm.jar ips $i &"
done