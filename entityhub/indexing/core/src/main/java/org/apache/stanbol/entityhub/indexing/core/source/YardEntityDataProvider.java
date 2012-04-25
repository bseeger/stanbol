package org.apache.stanbol.entityhub.indexing.core.source;

import java.util.Map;

import org.apache.stanbol.entityhub.indexing.core.EntityDataProvider;
import org.apache.stanbol.entityhub.indexing.core.IndexingDestination;
import org.apache.stanbol.entityhub.indexing.core.config.IndexingConfig;
import org.apache.stanbol.entityhub.servicesapi.model.Representation;
import org.apache.stanbol.entityhub.servicesapi.yard.Yard;
import org.apache.stanbol.entityhub.servicesapi.yard.YardException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link EntityDataProvider} implementation based on a {@link Yard}.
 * 
 * @author Rupert Westenthaler
 *
 */
public class YardEntityDataProvider implements EntityDataProvider {

    private static final Logger log = LoggerFactory.getLogger(YardEntityDataProvider.class);
    /**
     * If the {@link IndexingConfig#getIndexingDestination()} is used as
     * source.
     */
    public static final String PARAM_DESTINATION = "destination";
    
    private IndexingConfig indexingConfig;
    
    protected Yard yard;
    protected IndexingDestination indexingDest;

    public YardEntityDataProvider(){
        this(null);
    }
    public YardEntityDataProvider(Yard yard){
        this.yard = yard;
    }
    
    @Override
    public void setConfiguration(Map<String,Object> config) {
        indexingConfig = (IndexingConfig)config.get(IndexingConfig.KEY_INDEXING_CONFIG);
        if(yard == null && indexingConfig == null){
            throw new IllegalArgumentException("If the Yard is not parsed in" +
            		"the constructor the parsed configuration MUST contain a" +
            		"IndexingConfig!");
        }
    }

    @Override
    public boolean needsInitialisation() {
        return yard == null;
    }

    @Override
    public void initialise() {
        indexingDest = indexingConfig.getIndexingDestination();
        if(yard == null){
            if(indexingDest == null){
                throw new IllegalStateException("The IndexingConfig set in the" +
                		"configuration does not provide a valid indexing destination!");
            }
            yard = indexingDest.getYard();
            if(yard == null) {
                //TODO: we need to check if we might get in trouble because of the
                //      initialisation order. In this case we will need to use
                //      lazy initialisation on the first call to getEntityData
                throw new IllegalStateException("Unable to retieve Yard from the" +
                		"IndexingDestination!");
            }
        }
    }

    @Override
    public void close() {
        yard = null;
    }

    @Override
    public Representation getEntityData(String id) {
        try {
            return yard.getRepresentation(id);
        } catch (YardException e) {
            log.error("Unable to get Representation '"+id+"' from Yard",e);
            return null;
        } catch (IllegalArgumentException e) {
            log.error("Unable to get Representation '"+id+"' from Yard",e);
            return null;
        }
    }

}
