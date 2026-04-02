<div align="center">
 <h1>ReaperAPI</h1>
    
 <div>
  <a href="https://github.com/GrimAnticheat/ReaperAPI/actions/workflows/gradle-publish.yml">
   <img alt="Workflow" src="https://github.com/GrimAnticheat/ReaperAPI/actions/workflows/gradle-publish.yml/badge.svg" />
  </a>
  <a href="https://repo.grim.ac">
   <img alt="Maven repository" src="https://repo.grim.ac/api/badge/latest/snapshots/ac/grim/reaperac/ReaperAPI?name=Version&style=flat">
  </a>
  <a href="https://discord.grim.ac">
   <img alt="Discord" src="https://img.shields.io/discord/811396969670901800?style=flat&label=Discord&logo=discord">
  </a>
 </div>
 
 <br>
 <div>
  <a href="https://grim.ac">Website</a>
  |
  <a href="https://github.com/GrimAnticheat/Grim/wiki/Developer-API">Wiki</a>
  |
  <a href="https://repo.grim.ac/">Maven</a>
  |
  <a href="https://github.com/GrimAnticheat/Grim">ReaperAC</a>
 </div>

 <br>
 <div>
The official developer plugin API for GrimAnticheat
 </div>

</div>

### **Requirements**:
- Java 17 or higher
- A supported environment listed [here](https://github.com/GrimAnticheat/Grim/wiki/Supported-environments)


### Wiki
You can find more documentation and examples of how to use the API on the [wiki](https://github.com/GrimAnticheat/Grim/wiki/Developer-API).

### **Gradle**:
```kt
repositories {
    maven {
        name = "reaperacSnapshots"
        url = uri("https://repo.grim.ac/snapshots")
    }
}
dependencies {
    // replace %VERSION% with the latest API version
    compileOnly("ac.grim.reaperac:ReaperAPI:%VERSION%")
}
```

### **Maven**:
```xml
<repository>
  <id>reaperac-snapshots</id>
  <name>ReaperAC's Maven Repository</name>
  <url>https://repo.grim.ac/snapshots</url>
</repository>
<!-- replace %VERSION% with the latest API version -->
<dependency>
  <groupId>ac.grim.reaperac</groupId>
  <artifactId>ReaperAPI</artifactId>
  <version>%VERSION%</version>
  <scope>provided</scope>
</dependency>
```
