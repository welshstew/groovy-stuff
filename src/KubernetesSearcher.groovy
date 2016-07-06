import io.fabric8.kubernetes.api.model.Pod
import io.fabric8.kubernetes.api.model.PodList
import io.fabric8.kubernetes.client.DefaultKubernetesClient
import io.fabric8.kubernetes.client.Config

/**
 * See: https://github.com/fabric8io/kubernetes-client
 * Run this script by specifying the env vars as required in the link above..
 *
 * -Dkubernetes.auth.token=SMU7hlcDEaqquZth_BKOK5G0ToRPBScry_xTEPaR3k8
 *
 * Created by swinchester on 6/07/16.
 */
@GrabResolver(name='fusesource.m2', root='https://repo.fusesource.com/nexus/content/groups/public')
@GrabResolver(name='fusesource.ea', root='https://repo.fusesource.com/nexus/content/groups/ea')
@GrabResolver(name='redhat.ga', root='https://maven.repository.redhat.com/ga')
@Grab(group='io.fabric8', module='kubernetes-client', version='1.3.26.redhat-079')
@Grab(group='io.fabric8', module='kubernetes-api', version='2.2.0.redhat-079')
@Grab(group='io.fabric8', module='kubernetes-model', version='1.0.22.redhat-079')


//See: https://github.com/fabric8io/kubernetes-client

//run with -Dkubernetes.auth.token=SMU7hlcDEaqquZth_BKOK5G0ToRPBScry_xTEPaR3k8

def serverUrl = "https://10.1.2.2:8443"

DefaultKubernetesClient dkc = new DefaultKubernetesClient(serverUrl);

println dkc.getApiVersion()

PodList kubePods = dkc.inNamespace("jolokia").pods().withLabel("application","broker").list()

kubePods.items.each { pod ->
        println pod.metadata.name
}

println dkc.inNamespace("jolokia").pods().withLabel("application","broker").list()


