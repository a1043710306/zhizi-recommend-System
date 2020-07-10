package inveno.spider.common.view.model;

import java.io.Serializable;
import java.util.Date;

public class ProfileView implements Serializable
{
    private int id;
    private String profileName;
    private String pubCode;
    private String type;

    /**
     * Cron expression for this profile.
     */
    private String cronExpression;
    private String description;
    private String createUser;
    private Date createTime;
    private String updateUser;
    private Date updateTime;

    public ProfileView()
    {

    }

    public ProfileView(int id, String profileName, String pubCode, String type,
            String description, String updateUser)
    {
        this.id = id;
        this.profileName = profileName;
        this.pubCode = pubCode;
        this.type = type;
        this.description = description;
        this.updateUser = updateUser;
    }

    public Date getCreateTime()
    {
        return createTime;
    }

    public String getCreateUser()
    {
        return createUser;
    }

    public String getCronExpression()
    {
        return cronExpression;
    }

    public String getDescription()
    {
        return description;
    }

    public int getId()
    {
        return id;
    }

    public String getProfileName()
    {
        return profileName;
    }

    public String getPubCode()
    {
        return pubCode;
    }

    public String getType()
    {
        return type;
    }

    public Date getUpdateTime()
    {
        return updateTime;
    }

    public String getUpdateUser()
    {
        return updateUser;
    }

    public void setCreateTime(Date createTime)
    {
        this.createTime = createTime;
    }

    public void setCreateUser(String createUser)
    {
        this.createUser = createUser;
    }

    public void setCronExpression(String cronExpression)
    {
        this.cronExpression = cronExpression;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public void setId(int id)
    {
        this.id = id;
    }

    public void setProfileName(String profileName)
    {
        this.profileName = profileName;
    }

    public void setPubCode(String pubCode)
    {
        this.pubCode = pubCode;
    }

    public void setType(String type)
    {
        this.type = type;
    }

    public void setUpdateTime(Date updateTime)
    {
        this.updateTime = updateTime;
    }

    public void setUpdateUser(String updateUser)
    {
        this.updateUser = updateUser;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("profileName:").append(this.profileName).append("\t")
                .append("pubCode:").append(this.pubCode).append("\t")
                .append("experssion:").append(this.cronExpression);
        return sb.toString();
    }

}
