package com.apress.projpa2.chap9;

import java.util.*;
import javax.persistence.*;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import com.apress.projpa2.ProJPAUtil;

import examples.model.Department;
import examples.model.Employee;
import examples.model.Project;

/**
 * Pro JPA 2 Chapter 9 Criteria API
 *
 * Listing 9-6. In Expressions    - p.249

    mysql> use projpa2
    mysql> select * from EMPLOYEE;
    +----+-----------+--------+------------+------------+---------------+------------+
    | ID | NAME      | SALARY | STARTDATE  | ADDRESS_ID | DEPARTMENT_ID | MANAGER_ID |
    +----+-----------+--------+------------+------------+---------------+------------+
    |  1 | John      |  55000 | 2001-01-01 |          1 |             2 |          9 |
    |  2 | Rob       |  53000 | 2001-05-23 |          2 |             2 |          9 |
    |  3 | Peter     |  40000 | 2002-08-06 |          3 |             2 |          9 |
    |  4 | Frank     |  41000 | 2003-02-17 |          4 |             1 |         10 |
    |  5 | Scott     |  60000 | 2004-11-14 |          5 |             1 |         10 |
    |  6 | Sue       |  62000 | 2005-08-18 |          6 |             1 |         10 |
    |  7 | Stephanie |  54000 | 2006-06-07 |          7 |             1 |         10 |
    |  8 | Jennifer  |  45000 | 1999-08-11 |          8 |             1 |       NULL |
    |  9 | Sarah     |  52000 | 2002-04-26 |          9 |             2 |         10 |
    | 10 | Joan      |  59000 | 2003-04-16 |         10 |             1 |       NULL |
    | 11 | Marcus    |  35000 | 1995-07-22 |       NULL |          NULL |       NULL |
    | 12 | Joe       |  36000 | 1995-07-22 |       NULL |             3 |         11 |
    | 13 | Jack      |  43000 | 1995-07-22 |       NULL |             3 |       NULL |
    +----+-----------+--------+------------+------------+---------------+------------+
 
     mysql> select * from ADDRESS;
    +----+----------------+-------+---------------------+-------+
    | ID | CITY           | STATE | STREET              | ZIP   |
    +----+----------------+-------+---------------------+-------+
    |  1 | New York       | NY    | 123 Apple Tree Cr.  | 10001 |
    |  2 | Manhattan      | NY    | 654 Stanton Way.    | 10003 |
    |  3 | New York       | NY    | 99 Queen St.        | 10001 |
    |  4 | Redwood Shores | CA    | 445 McDonell Cr.    | 90123 |
    |  5 | San Jose       | CA    | 624 Hamilton Dr.    | 90123 |
    |  6 | San Jose       | CA    | 724 Coventry Rd.    | 90123 |
    |  7 | San Francisco  | CA    | 77 Manchester Blvd. | 90123 |
    |  8 | Moorestown     | NJ    | 53 King St.         | 08057 |
    |  9 | New York       | NY    | 14 Industrial Ave.  | 10001 |
    | 10 | Redwood Shores | CA    | 777 High Tech Ln.   | 90123 |
    +----+----------------+-------+---------------------+-------+
*
 */
public class Predicates_9_6_InExpressions {

    EntityManager em;

    public Predicates_9_6_InExpressions() {
        String unitName = "jpqlExamples"; // = args[0];
        EntityManagerFactory emf = Persistence.createEntityManagerFactory(unitName);
        em = emf.createEntityManager();
    }

    /**
     * Listing 9-6. In Expressions    - p.249
     *  SELECT e
        FROM Employee e
        WHERE e.address.state IN ('NY', 'CA')

        Note the chained invocation of the value() method in order to set multiple values into the IN expression.
        The argument to in() is the expression to search for against the list of values provided via the value() method.
     */
    public List<Employee> findEmployees(String city1, String city2) {

        System.out.println("findEmployees('" + city1 + "', '" + city2 + "')");
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Employee> c = cb.createQuery(Employee.class); // c - criteria query definition
        Root<Employee> emp = c.from(Employee.class);

        c.select(emp)
         .where(cb.in(emp.get("address").get("state")).value(city1).value(city2));

        TypedQuery<Employee> q = em.createQuery(c);
        return q.getResultList();
    }

    /**
     *  p.249
        In cases where there are a large number of value() calls to chain together that are all of the same type, the
        Expression interface offers a shortcut for creating IN expressions. The in() methods of this interface allow one or
        more values to be set in a single call.
     */
    public List<Employee> findEmployees2(String city1, String city2) {

        System.out.println("findEmployees2('" + city1 + "', '" + city2 + "')");
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Employee> c = cb.createQuery(Employee.class); // c - criteria query definition
        Root<Employee> emp = c.from(Employee.class);

        c.select(emp)
         .where(emp.get("address").get("state").in(city1, city2));

        TypedQuery<Employee> q = em.createQuery(c);
        return q.getResultList();
    }

    /**
     *  p.249
            SELECT e
            FROM Employee e
            WHERE e.department IN
            (SELECT DISTINCT d
            FROM Department d JOIN d.employees de JOIN de.project p
            WHERE p.name LIKE 'QA%')

            SELECT e FROM Employee e WHERE e.dept.id IN
                (SELECT DISTINCT d.id FROM Department d JOIN d.employees emp JOIN emp.projects empProj
                 WHERE empProj.name LIKE 'QA%'

        p.250 Listing 9-6. IN Expression Using a Subquery, code from below
        ~/projpa2/src/main/resources/examples/Chapter9/06-canonicalMetamodelQuery/src/model/examples/stateless/SearchService.java
        SearchService#getEmployeesUsingStringBasedQuery()
     */
    public List<Employee> findEmployees_9_6() {

        System.out.println("findEmployees_9_6()");
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Employee> c = cb.createQuery(Employee.class);
        Root<Employee> emp = c.from(Employee.class);
        Subquery<Integer> sq = c.subquery(Integer.class);
        Root<Department> dept = sq.from(Department.class);
        Join<Employee,Project> project = dept.join("employees").join("projects"); //dept.join(Department_.employees).join(Employee_.projects);

        sq.select(dept.<Integer>get("id"))
          .distinct(true)
          .where(cb.like(project.<String>get("name"), "QA%"));

        c.select(emp)
         .where(cb.in(emp.get("department").get("id")).value(sq)); // dept --> department

        TypedQuery<Employee> q = em.createQuery(c);
        return q.getResultList();
    }

    /**
        ~/projpa2/src/main/resources/examples/Chapter9/06-canonicalMetamodelQuery/src/model/examples/stateless/SearchService.java
        SearchService#getEmployeesUsingCanonicalMetamodelQuery()
     */
    /*
    public List<Employee> getEmployeesUsingCanonicalMetamodelQuery() {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Employee> c = cb.createQuery(Employee.class);
        Root<Employee> emp = c.from(Employee.class);
        Subquery<Integer> sq = c.subquery(Integer.class);
        Root<Department> dept = sq.from(Department.class);
        Join<Employee,Project> project = 
            dept.join(Department_.employees).join(Employee_.projects);
        sq.select(dept.get(Department_.id))
          .distinct(true)
          .where(cb.like(project.get(Project_.name), "QA%"));
        c.select(emp)
         .where(cb.in(emp.get(Employee_.dept).get(Department_.id)).value(sq));

        TypedQuery<Employee> q = em.createQuery(c);
        return q.getResultList();
    }
    */

    public static void main(String[] args) throws Exception {
        Predicates_9_6_InExpressions test = new Predicates_9_6_InExpressions();
        ProJPAUtil.printResult(test.findEmployees("NY", "CA"));
        ProJPAUtil.printResult(test.findEmployees("NY", "NJ"));
        ProJPAUtil.printResult(test.findEmployees2("NY", "NJ"));
        ProJPAUtil.printResult(test.findEmployees_9_6());
    }
}