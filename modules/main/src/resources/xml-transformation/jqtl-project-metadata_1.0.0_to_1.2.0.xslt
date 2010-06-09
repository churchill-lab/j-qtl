<!--
Stylesheet for transforming the project metadata XML from the 1.0.0 format
to the 1.2.0 format
-->
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:jax-r="http://jax.org/r"
                xmlns:j-qtl="http://jax.org/j-qtl">
    
    <xsl:output method="xml" indent="yes" encoding="iso-8859-1"/>
    
    <!-- copy by default -->
    <xsl:template match="*">
        <xsl:copy>
            <xsl:copy-of select="@*"/>
            <xsl:apply-templates/>
        </xsl:copy>
    </xsl:template>
    
    <!-- change the namespace of rHistoryItem from j-qtl to jax-r -->
    <xsl:template match="/j-qtl:jQtlProjectMetadata/j-qtl:rHistoryItem">
        <jax-r:rHistoryItem>
            <xsl:copy-of select="@*"/>
            <xsl:value-of select="."/>
        </jax-r:rHistoryItem>
        <xsl:apply-templates/>
    </xsl:template>
    
</xsl:stylesheet>
