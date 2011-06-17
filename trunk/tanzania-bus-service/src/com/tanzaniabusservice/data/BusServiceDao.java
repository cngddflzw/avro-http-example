package com.tanzaniabusservice.data;

import com.tanzaniabusservice.Company;

import java.util.List;

public interface BusServiceDao {
    Company loadCompanyById(long companyId);

    List<Company> listCompaniesAboveRating(float rating);
}
