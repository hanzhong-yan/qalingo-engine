/**
 * Most of the code in the Qalingo project is copyrighted Hoteia and licensed
 * under the Apache License Version 2.0 (release version 0.8.0)
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *                   Copyright (c) Hoteia, 2012-2014
 * http://www.hoteia.com - http://twitter.com/hoteia - contact@hoteia.com
 *
 */
package org.hoteia.qalingo.core.dao;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import javax.persistence.Query;

import org.apache.commons.lang.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.SQLQuery;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.sql.JoinType;
import org.hibernate.type.DoubleType;
import org.hibernate.type.LongType;
import org.hibernate.type.StringType;
import org.hoteia.qalingo.core.domain.MarketArea;
import org.hoteia.qalingo.core.domain.Retailer;
import org.hoteia.qalingo.core.domain.RetailerAddress;
import org.hoteia.qalingo.core.domain.RetailerAttribute;
import org.hoteia.qalingo.core.domain.RetailerCustomerComment;
import org.hoteia.qalingo.core.domain.RetailerCustomerRate;
import org.hoteia.qalingo.core.domain.RetailerTag;
import org.hoteia.qalingo.core.domain.Store;
import org.hoteia.qalingo.core.domain.bean.GeolocatedStore;
import org.hoteia.qalingo.core.fetchplan.FetchPlan;
import org.hoteia.qalingo.core.fetchplan.retailer.FetchPlanGraphRetailer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

@Repository("retailerDao")
public class RetailerDao extends AbstractGenericDao {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	// RETAILER

	public Retailer getRetailerById(final Long retailerId, Object... params) {
        Criteria criteria = createDefaultCriteria(Retailer.class);
        
        FetchPlan fetchPlan = handleSpecificRetailerFetchMode(criteria, params);
        
        criteria.add(Restrictions.eq("id", retailerId));
        
        Retailer retailer = (Retailer) criteria.uniqueResult();
        if(retailer != null){
            retailer.setFetchPlan(fetchPlan);
        }
        return retailer;
	}

    public Retailer getRetailerByCode(final String retailerCode, Object... params) {
        Criteria criteria = createDefaultCriteria(Retailer.class);

        FetchPlan fetchPlan = handleSpecificRetailerFetchMode(criteria, params);

        criteria.add(Restrictions.eq("code", handleCodeValue(retailerCode)));
        
        Retailer retailer = (Retailer) criteria.uniqueResult();
        if(retailer != null){
            retailer.setFetchPlan(fetchPlan);
        }
        return retailer;
    }
	
    public Long getMaxRetailerId() {
        Criteria criteria = createDefaultCriteria(Retailer.class);
        criteria.setProjection(Projections.max("id"));
        Long maxId = (Long)criteria.uniqueResult();
        return (maxId == null) ? new Long(0) : maxId;
    }
    
    public List<Retailer> findAllRetailers(Object... params) {
        Criteria criteria = createDefaultCriteria(Retailer.class);
        
        handleSpecificRetailerFetchMode(criteria, params);

        criteria.addOrder(Order.asc("code"));

        @SuppressWarnings("unchecked")
        List<Retailer> retailers = criteria.list();
        return retailers;
    }
    
    public List<Retailer> findRetailersByMarketAreaCode(final String marketAreaCode, Object... params) {
        Criteria criteria = createDefaultCriteria(MarketArea.class);
        
        criteria.add(Restrictions.eq("code", handleCodeValue(marketAreaCode)));
        MarketArea marketArea = (MarketArea) criteria.uniqueResult();

        List<Retailer> retailers = new ArrayList<Retailer>(marketArea.getRetailers());
        return retailers;
  }

    public List<Retailer> findRetailers(Object... params) {
        Criteria criteria = createDefaultCriteria(Retailer.class);
        
        handleSpecificRetailerFetchMode(criteria, params);

        criteria.addOrder(Order.asc("code"));

        @SuppressWarnings("unchecked")
        List<Retailer> retailers = criteria.list();
		return retailers;
	}
	
	public List<Retailer> findRetailersByTags(final List<String> tags, Object... params) {
        Criteria criteria = createDefaultCriteria(Retailer.class);

        handleSpecificRetailerFetchMode(criteria, params);

        criteria.createAlias("retailerTags", "tag", JoinType.LEFT_OUTER_JOIN);
        criteria.add(Restrictions.in("tag.code", tags));
        
        criteria.addOrder(Order.asc("name"));

        @SuppressWarnings("unchecked")
        List<Retailer> retailers = criteria.list();
		return retailers;
	}

	public List<Retailer> findLastRetailers(int maxResults, Object... params) {
        Criteria criteria = createDefaultCriteria(Retailer.class);
        
        handleSpecificRetailerFetchMode(criteria, params);

        criteria.addOrder(Order.desc("dateCreate"));

        @SuppressWarnings("unchecked")
        List<Retailer> retailers = criteria.list();
		return retailers;
	}
	
	public List<Retailer> findBestRetailersByQualityOfService(int maxResults, Object... params) {
        Criteria criteria = createDefaultCriteria(Retailer.class);
        
        handleSpecificRetailerFetchMode(criteria, params);

        criteria.addOrder(Order.desc("qualityOfService"));

        @SuppressWarnings("unchecked")
        List<Retailer> retailers = criteria.list();
		return retailers;
	}
	
	public List<Retailer> findBestRetailersByQualityPrice(int maxResults, Object... params) {
        Criteria criteria = createDefaultCriteria(Retailer.class);
        
        handleSpecificRetailerFetchMode(criteria, params);

        criteria.addOrder(Order.desc("ratioQualityPrice"));

        @SuppressWarnings("unchecked")
        List<Retailer> retailers = criteria.list();
		return retailers;
	}
	
	public List<Retailer> findRetailersByText(final String text, Object... params) {
        Criteria criteria = createDefaultCriteria(Retailer.class);
        
        handleSpecificRetailerFetchMode(criteria, params);

        criteria.add(Restrictions.or(Restrictions.like("code", text, MatchMode.ANYWHERE), Restrictions.like("name", text, MatchMode.ANYWHERE), Restrictions.like("description", text, MatchMode.ANYWHERE)));
        
        criteria.addOrder(Order.asc("id"));

        @SuppressWarnings("unchecked")
        List<Retailer> retailers = criteria.list();
		return retailers;
	}
	
	public Retailer saveOrUpdateRetailer(final Retailer retailer) {
		if(retailer.getDateCreate() == null){
            retailer.setDateCreate(new Date());
            if (retailer.getAddresses() != null && retailer.getAddresses().size() > 0) {
                for (Iterator<RetailerAddress> iterator = retailer.getAddresses().iterator(); iterator.hasNext();) {
                    RetailerAddress retailerAddress = (RetailerAddress) iterator.next();
                    retailerAddress.setDateCreate(new Date());
                    retailerAddress.setDateUpdate(new Date());
                }
            }
            if (retailer.getAttributes() != null && retailer.getAttributes().size() > 0) {
                for (Iterator<RetailerAttribute> iterator = retailer.getAttributes().iterator(); iterator.hasNext();) {
                    RetailerAttribute retailerAttribute = (RetailerAttribute) iterator.next();
                    retailerAttribute.setDateCreate(new Date());
                    retailerAttribute.setDateUpdate(new Date());
                }
            }
            if (retailer.getCustomerRates() != null && retailer.getCustomerRates().size() > 0) {
                for (Iterator<RetailerCustomerRate> iterator = retailer.getCustomerRates().iterator(); iterator.hasNext();) {
                    RetailerCustomerRate retailerCustomerRate = (RetailerCustomerRate) iterator.next();
                    retailerCustomerRate.setDateCreate(new Date());
                    retailerCustomerRate.setDateUpdate(new Date());
                }
            }
            if (retailer.getCustomerComments() != null && retailer.getCustomerComments().size() > 0) {
                for (Iterator<RetailerCustomerComment> iterator = retailer.getCustomerComments().iterator(); iterator.hasNext();) {
                    RetailerCustomerComment retailerCustomerComment = (RetailerCustomerComment) iterator.next();
                    retailerCustomerComment.setDateCreate(new Date());
                    retailerCustomerComment.setDateUpdate(new Date());
                }
            }
            if (retailer.getTags() != null && retailer.getTags().size() > 0) {
                for (Iterator<RetailerTag> iterator = retailer.getTags().iterator(); iterator.hasNext();) {
                    RetailerTag retailerTag = (RetailerTag) iterator.next();
                    retailerTag.setDateCreate(new Date());
                    retailerTag.setDateUpdate(new Date());
                }
            }
		}
        if (StringUtils.isEmpty(retailer.getCode())) {
            retailer.setCode(UUID.randomUUID().toString());
        }
		retailer.setDateUpdate(new Date());
        if (retailer.getId() != null) {
            if(em.contains(retailer)){
                em.refresh(retailer);
            }
            Retailer mergedRetailer = em.merge(retailer);
            em.flush();
            return mergedRetailer;
        } else {
            em.persist(retailer);
            return retailer;
        }
	}

	public void deleteRetailer(final Retailer retailer) {
		em.remove(retailer);
	}
	
    // RETAILER COMMENT/RATE
	
    public RetailerCustomerRate saveOrUpdateRetailerCustomerRate(final RetailerCustomerRate retailerCustomerRate) {
        if (retailerCustomerRate.getDateCreate() == null) {
            retailerCustomerRate.setDateCreate(new Date());
        }
        retailerCustomerRate.setDateUpdate(new Date());
        if (retailerCustomerRate.getId() != null) {
            if(em.contains(retailerCustomerRate)){
                em.refresh(retailerCustomerRate);
            }
            RetailerCustomerRate mergedRetailerCustomerRate = em.merge(retailerCustomerRate);
            em.flush();
            return mergedRetailerCustomerRate;
        } else {
            em.persist(retailerCustomerRate);
            return retailerCustomerRate;
        }
    }

	public void deleteRetailerCustomerRate(final RetailerCustomerRate retailerCustomerRate) {
		em.remove(retailerCustomerRate);
	}
	
	public RetailerCustomerComment saveOrUpdateRetailerCustomerComment(final RetailerCustomerComment retailerCustomerComment) {
		if(retailerCustomerComment.getDateCreate() == null){
			retailerCustomerComment.setDateCreate(new Date());
		}
		retailerCustomerComment.setDateUpdate(new Date());
        if (retailerCustomerComment.getId() != null) {
            if(em.contains(retailerCustomerComment)){
                em.refresh(retailerCustomerComment);
            }
            RetailerCustomerComment mergedRetailerCustomerComment = em.merge(retailerCustomerComment);
            em.flush();
            return mergedRetailerCustomerComment;
        } else {
            em.persist(retailerCustomerComment);
            return retailerCustomerComment;
        }
	}

	public void deleteRetailerCustomerComment(final RetailerCustomerComment retailerCustomerComment) {
		em.remove(retailerCustomerComment);
	}
	
	// STORE
	
	public Store getStoreById(final Long storeId, Object... params) {
        Criteria criteria = createDefaultCriteria(Store.class);
        
        FetchPlan fetchPlan = handleSpecificStoreFetchMode(criteria, params);

        criteria.add(Restrictions.eq("id", storeId));

        criteria.addOrder(Order.asc("code"));
        
        Store store = (Store) criteria.uniqueResult();
        if(store != null){
            store.setFetchPlan(fetchPlan);
        }
        return store;
	}

	public Store getStoreByCode(final String storeCode, Object... params) {
        Criteria criteria = createDefaultCriteria(Store.class);
        
        FetchPlan fetchPlan = handleSpecificStoreFetchMode(criteria, params);

        criteria.add(Restrictions.eq("code", handleCodeValue(storeCode)));

        criteria.addOrder(Order.asc("code"));
        
        Store store = (Store) criteria.uniqueResult();
        if(store != null){
            store.setFetchPlan(fetchPlan);
        }
		return store;
	}
	
    public Long getMaxStoreId() {
        Criteria criteria = createDefaultCriteria(Store.class);
        criteria.setProjection(Projections.max("id"));
        Long maxId = (Long)criteria.uniqueResult();
        return (maxId == null) ? new Long(0) : maxId;
    }
    
	public List<Store> findStores(Object... params) {
        Criteria criteria = createDefaultCriteria(Store.class);
        
        handleSpecificStoreFetchMode(criteria, params);
        
        criteria.addOrder(Order.asc("code"));

        @SuppressWarnings("unchecked")
        List<Store> stores = criteria.list();
		return stores;
	}
	
    public List<Store> findStoresByRetailerId(final Long retailerId, Object... params) {
        Criteria criteria = createDefaultCriteria(Store.class);

        handleSpecificStoreFetchMode(criteria, params);

        criteria.createAlias("retailer", "retailer", JoinType.LEFT_OUTER_JOIN);
        criteria.add( Restrictions.eq("retailer.id", retailerId));

        criteria.addOrder(Order.asc("name"));

        @SuppressWarnings("unchecked")
        List<Store> stores = criteria.list();
        return stores;
    }

    public List<Store> findStoresByRetailerCode(final String retailerCode, Object... params) {
        Criteria criteria = createDefaultCriteria(Store.class);

        handleSpecificStoreFetchMode(criteria, params);

        criteria.createAlias("retailer", "retailer", JoinType.LEFT_OUTER_JOIN);
        criteria.add( Restrictions.eq("retailer.code", retailerCode));
        
        criteria.addOrder(Order.asc("name"));

        @SuppressWarnings("unchecked")
        List<Store> stores = criteria.list();
        return stores;
    }
    
    public List<GeolocatedStore> findStoresByGeoloc(final String latitude, final String longitude, final String distance, int maxResults, Object... params) {
        Float latitudeFloat = new Float(latitude);
        Float longitudeFloat = new Float(longitude);
        String queryString = "SELECT store.id, store.code, ((ACOS(SIN(:latitude * PI() / 180) * SIN(latitude * PI() / 180) + COS(:latitude * PI() / 180) * COS(latitude * PI() / 180) * COS((:longitude - longitude) * PI() / 180)) * 180 / PI()) * 60 * 1.1515) AS distance FROM teco_store store HAVING distance <= :distanceValue ORDER BY distance ASC";
        Query query = createNativeQuery(queryString);
        query.setParameter("latitude", latitudeFloat.floatValue());
        query.setParameter("longitude", longitudeFloat.floatValue());
        query.setParameter("distanceValue", distance);
        query.setMaxResults(maxResults);
        query.unwrap(SQLQuery.class).addScalar("id", LongType.INSTANCE).addScalar("code", StringType.INSTANCE).addScalar("distance", DoubleType.INSTANCE);
        
        @SuppressWarnings("unchecked")
        List<Object[]> objects = query.getResultList();
        List<GeolocatedStore> stores = new ArrayList<GeolocatedStore>();
        for (Iterator<Object[]> iterator = objects.iterator(); iterator.hasNext();) {
            Object[] object = iterator.next();
            GeolocatedStore geolocatedStore = new GeolocatedStore();
            geolocatedStore.setId((Long)object[0]);
            geolocatedStore.setCode((String)object[1]);
            geolocatedStore.setDistance((Double)object[2]);
            stores.add(geolocatedStore);
        }
        return stores;
    }

    public List<GeolocatedStore> findStoresByGeolocAndCountry(final String countryCode, final String latitude, final String longitude, final String distance, int maxResults, Object... params) {
        Float latitudeFloat = new Float(latitude);
        Float longitudeFloat = new Float(longitude);
        String queryString = "SELECT store.id, store.code, ((ACOS(SIN(:latitude * PI() / 180) * SIN(latitude * PI() / 180) + COS(:latitude * PI() / 180) * COS(latitude * PI() / 180) * COS((:longitude - longitude) * PI() / 180)) * 180 / PI()) * 60 * 1.1515) AS distance FROM teco_store store WHERE country_code = :countryCode HAVING distance <= :distanceValue ORDER BY distance ASC";
        Query query = createNativeQuery(queryString);
        query.setParameter("latitude", latitudeFloat.floatValue());
        query.setParameter("longitude", longitudeFloat.floatValue());
        query.setParameter("countryCode", countryCode);
        query.setParameter("distanceValue", distance);
        query.setMaxResults(maxResults);
        query.unwrap(SQLQuery.class).addScalar("id", LongType.INSTANCE).addScalar("code", StringType.INSTANCE).addScalar("distance", DoubleType.INSTANCE);
        
        @SuppressWarnings("unchecked")
        List<Object[]> objects = query.getResultList();
        List<GeolocatedStore> stores = new ArrayList<GeolocatedStore>();
        for (Iterator<Object[]> iterator = objects.iterator(); iterator.hasNext();) {
            Object[] object = iterator.next();
            GeolocatedStore geolocatedStore = new GeolocatedStore();
            geolocatedStore.setId((Long)object[0]);
            geolocatedStore.setCode((String)object[1]);
            geolocatedStore.setDistance((Double)object[2]);
            stores.add(geolocatedStore);
        }
        return stores;
    }
    
	public Store saveOrUpdateStore(final Store store) {
		if(store.getDateCreate() == null){
			store.setDateCreate(new Date());
		}
		store.setDateUpdate(new Date());
        if (store.getId() != null) {
            if(em.contains(store)){
                em.refresh(store);
            }
            Store mergedStore = em.merge(store);
            em.flush();
            return mergedStore;
        } else {
            em.persist(store);
            return store;
        }
	}

	public void deleteStore(final Store store) {
		em.remove(store);
	}
	
    protected FetchPlan handleSpecificRetailerFetchMode(Criteria criteria, Object... params) {
        if (params != null && params.length > 0) {
            return super.handleSpecificFetchMode(criteria, params);
        } else {
            return super.handleSpecificFetchMode(criteria, FetchPlanGraphRetailer.defaultRetailerFetchPlan());
        }
    }
    
    protected FetchPlan handleSpecificStoreFetchMode(Criteria criteria, Object... params) {
        if (params != null && params.length > 0) {
            return super.handleSpecificFetchMode(criteria, params);
        } else {
            return super.handleSpecificFetchMode(criteria, FetchPlanGraphRetailer.defaultStoreFetchPlan());
        }
    }

}