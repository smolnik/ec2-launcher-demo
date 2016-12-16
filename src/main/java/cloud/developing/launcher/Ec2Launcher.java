package cloud.developing.launcher;

import java.util.ArrayList;
import java.util.List;

import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.CreateTagsRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.IamInstanceProfileSpecification;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.RunInstancesResult;
import com.amazonaws.services.ec2.model.Tag;
import com.amazonaws.waiters.Waiter;
import com.amazonaws.waiters.WaiterParameters;

/**
 * @author asmolnik
 *
 */
public class Ec2Launcher {

	public static void main(String[] args) {
		AmazonEC2 ec2 = new AmazonEC2Client(new ProfileCredentialsProvider("student"));
		RunInstancesRequest request = new RunInstancesRequest().withImageId("ami-a66221ce").withInstanceType("t2.micro").withMinCount(1)
				.withMaxCount(1).withKeyName("adamsmolnik-net-key-pair").withSecurityGroups("adamsmolnik.com")
				.withIamInstanceProfile(new IamInstanceProfileSpecification().withName("jee-role"));
		RunInstancesResult result = ec2.runInstances(request);
		Instance instance = result.getReservation().getInstances().get(0);

		List<Tag> tags = new ArrayList<>();
		tags.add(new Tag().withKey("Name").withValue("ec2-student000-from-java"));
		tags.add(new Tag().withKey("owner").withValue("000"));
		CreateTagsRequest ctr = new CreateTagsRequest();
		ctr.setTags(tags);
		ctr.withResources(instance.getInstanceId());
		ec2.createTags(ctr);
		System.out.println("waiting to get a new ec2 up and running...");
		Waiter<DescribeInstancesRequest> waiter = ec2.waiters().instanceRunning();
		waiter.run(new WaiterParameters<>(new DescribeInstancesRequest().withInstanceIds(instance.getInstanceId())));
		System.out.println("now up and running");
	}

}
