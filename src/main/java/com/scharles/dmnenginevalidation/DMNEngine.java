package com.scharles.dmnenginevalidation;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;

import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.Message.Level;
import org.kie.api.runtime.KieContainer;
import org.kie.dmn.api.core.DMNContext;
import org.kie.dmn.api.core.DMNModel;
import org.kie.dmn.api.core.DMNResult;
import org.kie.dmn.api.core.DMNRuntime;
import org.kie.dmn.api.marshalling.DMNMarshaller;
import org.kie.dmn.backend.marshalling.v1x.DMNMarshallerFactory;
import org.kie.dmn.model.api.Definitions;
import org.kie.internal.io.ResourceFactory;
import org.springframework.stereotype.Component;

/**
 * KieServices - The KieServices is a thread-safe singleton acting as a hub giving access to the other
 * Services provided by Kie.
 * 
 * The KieBase is a repository of all the application's knowledge definitions.
 * It will contain rules, processes, functions, type models. The KieBase itself
 * does not contain runtime data, instead sessions are created from the KieBase in which
 * data can be inserted and process instances started.
 * 
 * A KieModule is a container of all the resources necessary to define a set of KieBases like
 * a pom.xml defining its ReleaseId, a kmodule.xml file declaring the KieBases names and configurations
 * together with all the KieSession that can be created from them and all the other files
 * necessary to build the KieBases themselves
 * 
 * KieFileSystem is an in memory file system used to programmatically define
 * the resources composing a KieModule.
 * 
 * KieBuilder is a builder for the resources contained in a KieModule.
 * 
 * KieContainer is a container for all the KieBases of a given KieModule
 */
@Component
public class DMNEngine {

	private static final String FILE_PATH = "rules/LoanQualification.dmn";
	
	private final DMNMarshaller dmnMarshaller = DMNMarshallerFactory.newDefaultMarshaller();
	
    private KieContainer kieContainer;

    public void initialize() {
        KieServices kieServices = KieServices.Factory.get();
        KieFileSystem kieFileSystem = kieServices.newKieFileSystem();

        kieFileSystem.write(ResourceFactory.newClassPathResource(FILE_PATH));

        KieBuilder kieBuilder = kieServices.newKieBuilder(kieFileSystem);
        kieBuilder.buildAll();

        if (kieBuilder.getResults().hasMessages(Level.ERROR)) {
            throw new IllegalStateException("Error loading DMN rules in KieServices");
        }

        kieContainer = kieServices.newKieContainer(kieServices.getRepository().getDefaultReleaseId());
    }

    public DMNResult execute(Map<String, Income> executionContext) throws IllegalAccessException {
        DMNRuntime dmnRuntime = kieContainer.newKieSession().getKieRuntime(DMNRuntime.class);

		InputStream inputStream = this.getClass().getResourceAsStream("/" + FILE_PATH);
		try (InputStreamReader inputStreamReader = new InputStreamReader(inputStream)) {
			Definitions definitions = dmnMarshaller.unmarshal(inputStreamReader);

			DMNModel dmnModel = dmnRuntime.getModel(definitions.getNamespace(), definitions.getName());
			if (dmnModel == null) {
				throw new IllegalArgumentException(String.format("Couldn't find DMNModel with namespace '%s' and name '%s'",
						definitions.getNamespace(), definitions.getName()));
			}
	
			DMNContext dmnContext = dmnRuntime.newContext();
			executionContext.forEach(dmnContext::set);
			DMNResult dmnResult = dmnRuntime.evaluateAll(dmnModel, dmnContext);
	
			kieContainer.dispose();
	
			return dmnResult;
		} catch (Exception e) {
			throw new IllegalAccessException();
		}

    }
}
