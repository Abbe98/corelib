package eu.europeana.corelib;

import java.util.ArrayList;
import java.util.List;

import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.ServerAddress;
import java.net.UnknownHostException;

public class MongoProvider {

	private Mongo mongo;

	public MongoProvider(String hosts, String ports) {
		String[] hostList = hosts.split(",");
		String[] portList = ports.split(",");
		List<ServerAddress> serverAddresses = new ArrayList<>();
		int i = 0;
		for (String host : hostList) {
			if (host.length() > 0) {
				try {
					ServerAddress address = new ServerAddress(host,
							Integer.parseInt(portList[i]));
					serverAddresses.add(address);
				} catch (NumberFormatException | UnknownHostException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			i++;
		}
		
		
                MongoClientOptions.Builder builder = MongoClientOptions.builder();
                MongoClient client =new MongoClient(serverAddresses, builder.build());
                mongo = client;
		//mongo = new Mongo(serverAddresses,settings);
		
	}

	public Mongo getMongo() {
		return mongo;
	}
}
