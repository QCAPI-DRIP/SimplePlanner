import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;



public class Planner {

	
	////args[0] should be the path of the input file
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		File input = new File(args[0]);
		String dirName = args[1];
		BufferedReader in;
		try {
			in = new BufferedReader(new FileReader(input));
			String line = null;
			boolean check = false;
			ArrayList<String> dockerNames = new ArrayList<String>();
			while((line = in.readLine()) != null){
				if(line.contains("topology_template")){
					check = true;
				}
				if(check)
				{
					if(line.contains("file")){
						String content = line.trim().replace('\"', ' ');
						String [] cs = content.split(":");
						String docker_name = cs[1].trim();
						dockerNames.add(docker_name);
					}
				}
				if(line.contains("tosca_definitions_version")
						|| line.contains("description")
						|| line.contains("repositories")
						|| line.contains("artifact_types") || line.contains("data_types")
						|| line.contains("node_types"))
					check = false;
			}
			in.close();
			
			String exampleFilePath = getCurrentDir()+"example_a.yml";
			File example = new File(exampleFilePath);
			in = new BufferedReader(new FileReader(example));
			
			String block = ""; 
			String head = "";
			
			boolean block_b = false;
			while((line = in.readLine()) != null){
				if(line.contains("components")){
					block_b = true;
					continue;
				}
				if(block_b)
					block += line+"\n";
				if(!block_b)
					head += line+"\n";
			}
			
			in.close();
			UUID fuuid = UUID.randomUUID();
			String file_guid = fuuid.toString();
			String newDir = getCurrentDir()+dirName+"/";
			Process p = Runtime.getRuntime().exec("mkdir "+newDir);
			try {
				p.waitFor();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			String outfPath = getCurrentDir()+dirName+"/"+file_guid+".yml";
			FileWriter outputf = new FileWriter(outfPath);
			outputf.write(head);
			outputf.write("components:\n");
			for(int i = 0 ; i<dockerNames.size() ; i++){
				UUID uuid = UUID.randomUUID();
				String name_guid = uuid.toString();
				String privateAddress = "192.168.10."+(i+10);
				if(i == 0)
					outputf.write(generateVM(block, name_guid, dockerNames.get(i), privateAddress, "master")); 
				else
					outputf.write(generateVM(block, name_guid, dockerNames.get(i), privateAddress, "slave")); 
			}
			outputf.close();
			
			String allFilePath = getCurrentDir()+dirName+"/"+"planner_output_all.yml";
			outputf = new FileWriter(allFilePath);
			outputf.write("topologies:\n");
			outputf.write("  - topology: "+file_guid+"\n");
			outputf.write("    cloudProvider: EC2\n");
			outputf.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		

	}
	
	
	private static String getCurrentDir(){
		String curDir = new Planner().getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
		int index = curDir.lastIndexOf('/');
		return curDir.substring(0, index+1);
	}

	private static String generateVM(String block, String nodeName, String dockerName, String privateAddress, String role){
		block = block.replaceAll("nodeA", nodeName);
		block = block.replaceAll("DOCKER", "\""+dockerName+"\"");
		block = block.replaceAll("192.168.10.10", privateAddress);
		block = block.replaceAll("ROLE", role);
		return block;
	}
}
