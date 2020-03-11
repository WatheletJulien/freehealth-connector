package org.taktik.connector.business.recipeprojects.core.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.taktik.connector.business.recipeprojects.core.domain.KgssIdentifierType;
import org.taktik.connector.business.recipeprojects.core.exceptions.IntegrationModuleException;
import org.taktik.connector.technical.exception.TechnicalConnectorException;
import org.taktik.connector.technical.service.etee.domain.EncryptionToken;
import org.taktik.connector.technical.service.keydepot.KeyDepotService;

public class ETKHelper {

    private static final Logger LOG = LogManager.getLogger(ETKHelper.class);
    private static final String RECIPE_ID = "0823257311";
    private static final String KGSS_ID = "0809394427";

    private Map<String, EncryptionToken> etkCache = new HashMap<String, EncryptionToken>();
    private Map<String, List<EncryptionToken>> etksCache = new HashMap<String, List<EncryptionToken>>();
    private KeyDepotService keyDepotService;

    public ETKHelper(KeyDepotService keyDepotService) {
        this.keyDepotService = keyDepotService;
    }

    public List<EncryptionToken> getKGSS_ETK() throws IntegrationModuleException {
        return getEtks(KgssIdentifierType.CBE, KGSS_ID, "KGSS");
    }

    public List<EncryptionToken> getRecipe_ETK() throws IntegrationModuleException {
        return getEtks(KgssIdentifierType.CBE, RECIPE_ID, "");
    }

    public List<EncryptionToken> getEtks(KgssIdentifierType identifierType, String identifierValue) throws IntegrationModuleException {
        return getEtks(identifierType, identifierValue, "");
    }

    private List<EncryptionToken> getEtks(KgssIdentifierType identifierType, String identifierValue, String application) throws IntegrationModuleException {
        try {
            return new ArrayList<>(this.keyDepotService.getETKSet(org.taktik.connector.technical.utils.IdentifierType.valueOf(identifierType.name()), identifierValue, application, null, false));
        } catch (TechnicalConnectorException e) {
            throw new IntegrationModuleException(e);
        }
    }
}
