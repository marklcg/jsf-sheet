# jsf-sheet
The JSF Sheet Component is a JSF 2 component for the Primefaces Framework that wrappers the excellent
[Hansontable](https://handsontable.com/) JavaScript component.

The project provides a JSF Spreadsheet component with the following features:

* Fast In-Cell Editing
* Copy/Paste
* Range Selection including Copy/Paste and Fill
* Sorting
* Filtering
* Fixed Columns and Rows
* Ajax Updates and Validation

To use the component, add the following dependency to your maven project:

```xml
<dependency>
    <groupId>com.lassitercg</groupId>
    <artifactId>jsf-sheet</artifactId>
    <version>1.3</version>
</dependency>
```

Add the XML namespace to your XHTML page:

```xml
xmlns:lcg="http://www.lassitercg.com/jsf"
```

And use the component:

```xml
<h:form id="frmMain">
    <lcg:sheet id="sheet" value="#{assets}" var="row" height="400" label="Sheet" rowKey="#{row.assetId}"
               fixedCols="2" showRowHeaders="false" sortBy="#{row.assetId}" sortOrder="ascending">

        <p:ajax listener="#{assetController.cellEditEvent}"/>

        <f:facet name="header">
            <h:outputText value="Assets"/>
        </f:facet>

        <f:facet name="footer">
            <h:outputText value="#{assets.size()} Records"/>
        </f:facet>

        <lcg:column headerText="Assset Id" readonly="true" value="#{row.assetId}" sortBy="#{row.assetId}"
                    colWidth="100" filterBy="#{row.assetId}"/>
        <lcg:column headerText="Host Name" value="#{row.hostName}" colWidth="200" sortBy="#{row.hostName}"
                    filterBy="#{row.hostName}"/>
        <lcg:column headerText="Type" value="#{row.assetType}" readonly="true" colWidth="100"
                    sortBy="#{row.assetType}" filterBy="#{row.assetType}" filterOptions="#{assetTypes}"/>
        <lcg:column headerText="Platform" value="#{row.platform}" readonly="true" colWidth="100"
                    sortBy="#{row.platform}" filterBy="#{row.platform}" filterOptions="#{platformTypes}"/>
        <lcg:column headerText="Arch" value="#{row.platformArch}" readonly="true" colWidth="100"
                    sortBy="#{row.platformArch}" filterBy="#{row.platformArch}" filterOptions="#{archTypes}"/>
        <lcg:column headerText="Comments" value="#{row.comment}" colWidth="200" sortBy="#{row.comment}"/>
        <lcg:column headerText="Updated" value="#{row.lastUpdated}" colWidth="150" sortBy="#{row.lastUpdated}">
            <f:convertDateTime type="both"/>
        </lcg:column>
        <lcg:column headerText="Total" value="#{row.total}" readonly="true" colWidth="100"/>
        <lcg:column headerText="Value 1" value="#{row.value1}" colWidth="100"/>
        <lcg:column headerText="Value 2" value="#{row.value2}" colWidth="100"/>
        <lcg:column headerText="Value 3" value="#{row.value3}" colWidth="100"/>
        <lcg:column headerText="Value 4" value="#{row.value4}" colWidth="100"/>
        <lcg:column headerText="Value 5" value="#{row.value5}" colWidth="100"/>
        <lcg:column headerText="Value 6" value="#{row.value6}" colWidth="100"/>
        <lcg:column headerText="Value 7" value="#{row.value7}" colWidth="100"/>
        <lcg:column headerText="Value 8" value="#{row.value8}" colWidth="100"/>
        <lcg:column headerText="Value 9" value="#{row.value9}" colWidth="100"/>
        <lcg:column headerText="Value 10" value="#{row.value10}" colWidth="100"/>
    </lcg:sheet>
</h:form>
```

Some sample backing beans:
```java
@ApplicationScoped
public class AssetProducer implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	AssetManager manager;

	private List<Asset> assets;

	@PostConstruct
	void refresh() {
		assets = manager.listAll();
		if (assets.isEmpty()) {
			manager.initTestData();
			assets = manager.listAll();
		}
	}

	@Produces
	@Named
	public List<Asset> getAssets() {
		if (assets == null)
			refresh();
		return assets;
	}
}
```

```java
@Stateless
public class AssetManager {

	@Inject
	EntityManager em;

	@TransactionAttribute(TransactionAttributeType.SUPPORTS)
	public List<Asset> listAll() {
		TypedQuery<Asset> qry = em.createQuery("SELECT a from Asset a", Asset.class);
		return qry.getResultList();
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public void persist(Asset asset) {
		em.persist(asset);
	}

	protected void addAssets(int count, PlatformType platform, PlatformArchType arch, AssetType type) {
		for (int i = 0; i < count; i++) {
			Asset asset = new Asset();
			asset.setPlatform(platform);
			asset.setPlatformArch(arch);
			asset.setHostName(type.toString().toLowerCase() + i + ".example.lan");
			asset.setAssetType(type);
			asset.setLastUpdated(new Date());
			persist(asset);
		}
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public void initTestData() {
		System.out.println("Initializing Test Data ...");
		addAssets(10, PlatformType.Linux, PlatformArchType.AMD64, AssetType.SERVER);
		addAssets(100, PlatformType.Windows, PlatformArchType.I386, AssetType.DESKTOP);
		addAssets(5, null, null, AssetType.PRINTER);
		em.flush();
		System.out.println("Done.");
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public Asset update(Asset asset) {
		return em.merge(asset);
	}

}
```

```java
@Entity
public class Asset implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long assetId;

	@Temporal(TemporalType.DATE)
	private Date purchaseDate;

	@Column(length = 200)
	private String hostName;

	@Column(length = 200)
	private String description;

	@Column(precision = 10, scale = 2)
	private BigDecimal purchasePrice;

	@Enumerated(EnumType.STRING)
	private PlatformType platform;

	@Column(length = 20)
	@Enumerated(EnumType.STRING)
	private PlatformArchType platformArch;

	@Enumerated(EnumType.STRING)
	@Column(length = 20)
	private AssetType assetType;

	@Column(length = 200)
	private String comment;

	@Temporal(TemporalType.TIMESTAMP)
	private Date lastUpdated;

	@Column(length = 20)
	private String patchLevel;

	@Column(length = 40)
	private String allocatedTo;

	private String location;

	private Integer value1;

	private Integer value2;

	private Integer value3;

	private Integer value4;

	private Integer value5;

	private Integer value6;

	private Integer value7;

	private Integer value8;

	private Integer value9;

	private Integer value10;

	public Long getAssetId() {
		return assetId;
	}

	public void setAssetId(Long assetId) {
		this.assetId = assetId;
	}

	public Date getPurchaseDate() {
		return purchaseDate;
	}

	public void setPurchaseDate(Date purchaseDate) {
		this.purchaseDate = purchaseDate;
	}

	public String getHostName() {
		return hostName;
	}

	public void setHostName(String hostName) {
		this.hostName = hostName;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public BigDecimal getPurchasePrice() {
		return purchasePrice;
	}

	public void setPurchasePrice(BigDecimal purchasePrice) {
		this.purchasePrice = purchasePrice;
	}

	public PlatformType getPlatform() {
		return platform;
	}

	public void setPlatform(PlatformType platform) {
		this.platform = platform;
	}

	public PlatformArchType getPlatformArch() {
		return platformArch;
	}

	public void setPlatformArch(PlatformArchType platformArch) {
		this.platformArch = platformArch;
	}

	public AssetType getAssetType() {
		return assetType;
	}

	public void setAssetType(AssetType assetType) {
		this.assetType = assetType;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public Date getLastUpdated() {
		return lastUpdated;
	}

	public void setLastUpdated(Date lastUpdated) {
		this.lastUpdated = lastUpdated;
	}

	public String getPatchLevel() {
		return patchLevel;
	}

	public void setPatchLevel(String patchLevel) {
		this.patchLevel = patchLevel;
	}

	public String getAllocatedTo() {
		return allocatedTo;
	}

	public void setAllocatedTo(String allocatedTo) {
		this.allocatedTo = allocatedTo;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public Integer getValue1() {
		return value1;
	}

	public void setValue1(Integer value1) {
		this.value1 = value1;
	}

	public Integer getValue2() {
		return value2;
	}

	public void setValue2(Integer value2) {
		this.value2 = value2;
	}

	public Integer getValue3() {
		return value3;
	}

	public void setValue3(Integer value3) {
		this.value3 = value3;
	}

	public Integer getValue4() {
		return value4;
	}

	public void setValue4(Integer value4) {
		this.value4 = value4;
	}

	public Integer getValue5() {
		return value5;
	}

	public void setValue5(Integer value5) {
		this.value5 = value5;
	}

	public Integer getValue6() {
		return value6;
	}

	public void setValue6(Integer value6) {
		this.value6 = value6;
	}

	public Integer getValue7() {
		return value7;
	}

	public void setValue7(Integer value7) {
		this.value7 = value7;
	}

	public Integer getValue8() {
		return value8;
	}

	public void setValue8(Integer value8) {
		this.value8 = value8;
	}

	public Integer getValue9() {
		return value9;
	}

	public void setValue9(Integer value9) {
		this.value9 = value9;
	}

	public Integer getValue10() {
		return value10;
	}

	public void setValue10(Integer value10) {
		this.value10 = value10;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((assetId == null) ? 0 : assetId.hashCode());
		result = prime * result + ((hostName == null) ? 0 : hostName.hashCode());
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Asset other = (Asset) obj;
		if (assetId == null) {
			if (other.assetId != null)
				return false;
		} else if (!assetId.equals(other.assetId))
			return false;
		if (hostName == null) {
			if (other.hostName != null)
				return false;
		} else if (!hostName.equals(other.hostName))
			return false;
		return true;
	}

	public Integer getTotal() {
		return (value1 == null ? 0 : value1) +
				(value2 == null ? 0 : value2) +
				(value3 == null ? 0 : value3) +
				(value4 == null ? 0 : value4) +
				(value5 == null ? 0 : value5) +
				(value6 == null ? 0 : value6) +
				(value7 == null ? 0 : value7) +
				(value8 == null ? 0 : value8) +
				(value9 == null ? 0 : value9) +
				(value10 == null ? 0 : value10);
	}
}
```

