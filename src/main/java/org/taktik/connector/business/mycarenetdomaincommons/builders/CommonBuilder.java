package org.taktik.connector.business.mycarenetdomaincommons.builders;

import org.taktik.connector.business.common.domain.Patient;
import org.taktik.connector.business.mycarenetdomaincommons.domain.CommonInput;
import org.taktik.connector.business.mycarenetdomaincommons.domain.McnPackageInfo;
import org.taktik.connector.business.mycarenetdomaincommons.domain.Origin;
import org.taktik.connector.business.mycarenetdomaincommons.domain.Routing;
import org.taktik.connector.technical.config.util.domain.PackageInfo;
import org.taktik.connector.technical.exception.TechnicalConnectorException;
import org.taktik.connector.technical.utils.ConfigurableImplementation;
import org.joda.time.DateTime;

public interface CommonBuilder extends ConfigurableImplementation {
   /** @deprecated */
   @Deprecated
   CommonInput createCommonInput(PackageInfo var1, boolean var2, String var3) throws TechnicalConnectorException;

}
