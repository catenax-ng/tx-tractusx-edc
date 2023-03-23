package org.eclipse.tractusx.edc.lifecycle;

import org.eclipse.edc.connector.contract.spi.offer.store.ContractDefinitionStore;
import org.eclipse.edc.connector.policy.spi.store.PolicyDefinitionStore;
import org.eclipse.edc.spi.asset.AssetIndex;
import org.eclipse.edc.spi.query.QuerySpec;
import org.eclipse.edc.spi.system.ServiceExtensionContext;

import java.util.stream.Collectors;

/**
 * Helper class to delete all objects from a runtime's data stores.
 */
public class DataWiper {

    private final ServiceExtensionContext context;

    public DataWiper(ServiceExtensionContext context) {
        this.context = context;
    }

    public void clearPersistence() {
        clearAssetIndex();
        clearPolicies();
        clearContractDefinitions();
    }

    public void clearContractDefinitions() {
        var cds = context.getService(ContractDefinitionStore.class);
        cds.findAll(QuerySpec.max()).forEach(cd -> cds.deleteById(cd.getId()));
    }

    public void clearPolicies() {
        var ps = context.getService(PolicyDefinitionStore.class);
        // must .collect() here, otherwise we'll get a ConcurrentModificationException
        ps.findAll(QuerySpec.max()).collect(Collectors.toList()).forEach(p -> ps.deleteById(p.getId()));
    }

    public void clearAssetIndex() {
        var index = context.getService(AssetIndex.class);
        index.queryAssets(QuerySpec.max()).forEach(asset -> index.deleteById(asset.getId()));
    }
}