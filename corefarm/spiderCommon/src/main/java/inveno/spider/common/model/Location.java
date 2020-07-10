package inveno.spider.common.model;

public class Location implements Comparable<Location>
{
    private String country;
    private String state;
    private String city;
    private int weight;

    public Location(String state, int weight)
    {
        this.state = state;
        this.weight = weight;
        this.country = "中国";
    }

    public Location(String state, String city, int weight)
    {
        this(state, weight);
        this.city = city;
    }

    public Location(String country, String state, String city, int weight)
    {
        this(state, city, weight);
        this.country = country;
    }

    public String getCountry()
    {
        return country;
    }

    public String getState()
    {
        return state;
    }

    public String getCity()
    {
        return city;
    }

    public int getWeight()
    {
        return weight;
    }

    @Override
    public int compareTo(Location o)
    {
        return this.country.equalsIgnoreCase(o.getCountry())
                && this.state.equalsIgnoreCase(o.getState())
                && this.city.equalsIgnoreCase(o.getCity()) ? 0 : -1;
    }
    @Override
    public boolean equals(Object obj)
    {
        if(obj instanceof Location)
        {
            Location o = (Location)obj;
            
            return this.country.equalsIgnoreCase(o.getCountry())
                    && this.state.equalsIgnoreCase(o.getState())
                    && this.city.equalsIgnoreCase(o.getCity());
        }else
        {
            return false;
        }
    }

}
